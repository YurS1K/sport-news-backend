import random
import re
import time
from concurrent.futures import ThreadPoolExecutor
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

MAX_WORKERS = 8
REQUEST_DELAY_RANGE = (0.2, 0.6)
REQUEST_TIMEOUT = 15
CLASSIFIER_BATCH_SIZE = 16

BLACKLIST = {'Чемпионата»', 'Чемпионат»', 'Чемпионата', 'Чемпионат'}

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 '
                  '(KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
    'Accept-Language': 'ru-RU,ru;q=0.9',
}


def _build_session():
    session = requests.Session()
    retry = Retry(
        total=3,
        backoff_factor=1.0,
        status_forcelist=(429, 500, 502, 503, 504),
        allowed_methods=frozenset(['GET']),
        respect_retry_after_header=True,
    )
    adapter = HTTPAdapter(
        pool_connections=MAX_WORKERS,
        pool_maxsize=MAX_WORKERS,
        max_retries=retry,
    )
    session.mount('https://', adapter)
    session.mount('http://', adapter)
    session.headers.update(HEADERS)
    return session


def clean_text_for_ner(text: str) -> str:
    text = re.sub(r'[«»"“”\']', '', text)
    return re.sub(r'\s+', ' ', text).strip()


def format_date(date: str) -> str:
    months = {
        'января': '01', 'февраля': '02', 'марта': '03', 'апреля': '04',
        'мая': '05', 'июня': '06', 'июля': '07', 'августа': '08',
        'сентября': '09', 'октября': '10', 'ноября': '11', 'декабря': '12',
    }
    try:
        day, month_name, year_time = date.split(' ', 2)
        year, time_part = year_time.split(', ')
        time_part = time_part.replace(' МСК', '')
        return f"{time_part} {day}.{months[month_name]}.{year}"
    except Exception:
        return date


def _fetch(session, url):
    time.sleep(random.uniform(*REQUEST_DELAY_RANGE))
    try:
        response = session.get(url, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        return response.text
    except Exception as e:
        print(f"Ошибка при скачивании {url}: {e}")
        return None


def _parse_article_html(html: str):
    soup = BeautifulSoup(html, 'lxml')

    content = soup.find('div', class_='article-content')
    if not content:
        return None

    parags = [p.get_text(strip=False) for p in content.find_all('p')]
    text = ' '.join(parags).replace('""', '"').strip()
    if not text:
        return None

    tags_container = soup.find('div', class_='tags__items js-tags-items')
    tags = [t.get_text(strip=True) for t in tags_container.find_all('a')] if tags_container else []

    date_tag = soup.find('time', class_='article-head__date')
    date = date_tag.get_text(strip=True) if date_tag else ''

    author_tag = soup.find('div', class_='article-head__author-name')
    author = author_tag.get_text(strip=True) if author_tag else 'Нет автора'

    return {
        'tags': tags[1:],
        'date': date,
        'author': author,
        'text': text,
    }


def _run_nlp(nlp, texts):
    cleaned = [clean_text_for_ner(t) for t in texts]
    try:
        docs = nlp.bulk_process(cleaned)
    except Exception as e:
        print(f"bulk_process не сработал ({e}), переключаюсь на пошаговый режим")
        docs = [nlp(t) for t in cleaned]

    results = []
    for doc in docs:
        ents = set()
        for sentence in doc.sentences:
            for ent in sentence.ents:
                if ent.text not in BLACKLIST:
                    ents.add(ent.text)
        results.append(ents)
    return results


def _run_classifier(classifier, texts):
    outputs = classifier(texts, batch_size=CLASSIFIER_BATCH_SIZE, truncation=True)
    return [o['label'] for o in outputs]


def parse_championat(url, nlp, classifier):
    session = _build_session()

    try:
        response = session.get(url, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
    except Exception as e:
        print(f"Ошибка при загрузке списка: {e}")
        return []

    soup = BeautifulSoup(response.text, 'lxml')
    news_container = soup.find('div', class_='news-items')
    if not news_container:
        return []

    items = []
    for item in news_container.find_all('div', class_='news-item', limit=300):
        title_tag = item.find('a', class_='news-item__title')
        if not title_tag or not title_tag.get('href'):
            continue
        items.append({
            'title': title_tag.get_text(strip=True),
            'link': urljoin(url, title_tag['href']),
        })

    if not items:
        return []

    htmls = [None] * len(items)
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as pool:
        results_iter = pool.map(lambda u: _fetch(session, u), [it['link'] for it in items])
        for idx, html in enumerate(results_iter):
            htmls[idx] = html

    parsed_articles = []
    valid_indices = []
    for idx, html in enumerate(htmls):
        if html is None:
            continue
        parsed = _parse_article_html(html)
        if parsed is None:
            continue
        parsed_articles.append(parsed)
        valid_indices.append(idx)

    if not parsed_articles:
        return []

    texts = [a['text'] for a in parsed_articles]
    ents_list = _run_nlp(nlp, texts)
    sentiments = _run_classifier(classifier, texts)

    parsed_data = []
    for k, idx in enumerate(valid_indices):
        article = parsed_articles[k]
        parsed_data.append({
            'title': items[idx]['title'],
            'link': items[idx]['link'],
            'tags': list(set(filter(None, article['tags']))),
            'date': format_date(article['date']),
            'author': article['author'],
            'text': article['text'],
            'entities': ents_list[k],
            'sentiment': sentiments[k],
            'source': 'CHAMPIONAT',
        })

    return parsed_data
