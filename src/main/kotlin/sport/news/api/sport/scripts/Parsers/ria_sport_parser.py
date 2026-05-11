import re
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin


def clean_ria_text(text):
    pattern = r'^[А-ЯA-Z][^,]*?,\s+\d+\s+[а-я]+\s+[—–-]\s+РИА Новости(?:,\s+[А-Я][а-я]+\s+[А-Я][а-я]+)?[.:]?\s*'
    return re.sub(pattern, '', text, flags=re.MULTILINE).strip()


def clean_text_for_ner(text: str) -> str:
    text = re.sub(r'[«»"“”\']', '', text)
    return re.sub(r'\s+', ' ', text).strip()


def parse_ria_sport_tags(article_url, headers, nlp, classifier):
    try:
        response = requests.get(article_url, headers=headers)
        response.raise_for_status()
        article_soup = BeautifulSoup(response.text, 'html.parser')

        tags_container = article_soup.find('div', class_='article__tags')
        if tags_container:
            tags = [tag.get_text(strip=True) for tag in tags_container.find_all('a')]
        else:
            tags = []

        date = article_soup.find('div', class_='article__info-date').get_text(strip=True)
        author_tag = article_soup.find('div', class_='article__author-name')
        author = author_tag.get_text(strip=True) if author_tag else "Нет автора"

        parags = [p.get_text(strip=False) for p in article_soup.find_all('div', class_='article__text')]
        text = " ".join(parags).replace('""', '"')
        text = clean_ria_text(text)

        text_lemma = clean_text_for_ner(text)
        doc = nlp(text_lemma)

        ents = set()
        for sentence in doc.sentences:
            for ent in sentence.ents:
                ents.add(ent.text)

        sentiment = classifier(text)
        return tags, date, author, text, ents, sentiment[0]['label']

    except Exception as e:
        print(f"Ошибка при парсинге статьи: {str(e)}")
        return []


def parse_ria_sport(url, nlp, classifier):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                      'Chrome/91.0.4472.124 Safari/537.36',
        'Accept-Language': 'ru-RU,ru;q=0.9'
    }

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')
        news_container = soup.find('div', class_='list')
        if not news_container:
            return []

        news_items = news_container.find_all('div', class_='list-item')
        parsed_data = []

        for item in news_items:
            title_tag = item.find('a', class_='list-item__title')
            title = title_tag.get_text(strip=True) if title_tag else 'Без заголовка'
            print(f"Парсинг статьи RIA {title}")
            link = urljoin(url, title_tag['href']) if title_tag else ''

            tags, date, author, text, ents, sentiment = parse_ria_sport_tags(link, headers, nlp, classifier) if link else []

            tags = list(set(filter(None, tags)))
            date = date[:16] if len(date) > 16 else date

            parsed_data.append({
                'title': title,
                'link': link,
                'tags': tags,
                'date': date,
                'author': author,
                'text': text,
                'entities': ents,
                'sentiment': sentiment,
                'source': 'RIA',
            })

        return parsed_data
    except Exception as e:
        print(f"Ошибка: {str(e)}")
        return []

