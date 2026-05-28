import re
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin


def clean_text_for_ner(text: str) -> str:
    text = re.sub(r'[«»"“”\']', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text


def parse_championat_tags(article_url, headers, nlp, classifier):
    blacklist = ['Чемпионата»', 'Чемпионат»', 'Чемпионата', 'Чемпионат']
    try:
        response = requests.get(article_url, headers=headers)
        response.raise_for_status()
        article_soup = BeautifulSoup(response.text, 'html.parser')

        tags_container = article_soup.find('div', class_='tags__items js-tags-items')
        if tags_container:
            tags = [tag.get_text(strip=True) for tag in tags_container.find_all('a')]
        else:
            tags = []
        date = article_soup.find('time', class_="article-head__date").get_text(strip=True)
        author_tag = article_soup.find('div', class_="article-head__author-name")
        author = author_tag.get_text(strip=True) if author_tag else "Нет автора"

        content_div = article_soup.find('div', class_='article-content')

        for br in content_div.find_all('br'):
            br.replace_with('\n')

        parags = [p.get_text(strip=False) for p in content_div.find_all('p')]

        text = "\n".join(parags).replace('""', '"')

        text_lemma = clean_text_for_ner(text)

        ents = set()
        doc = nlp(text_lemma)
        for sentence in doc.sentences:
            for ent in sentence.ents:
                entity_text = ent.text

                if entity_text in blacklist:
                    continue

                ents.add(entity_text)

        sentiment = classifier(text)
        return tags[1:], date, author, text, ents, sentiment[0]['label']

    except Exception as e:
        print(f"Ошибка при парсинге статьи: {str(e)}")
        return []

def format_date(date):
    months = {
        'января': '01', 'февраля': '02', 'марта': '03', 'апреля': '04',
        'мая': '05', 'июня': '06', 'июля': '07', 'августа': '08',
        'сентября': '09', 'октября': '10', 'ноября': '11', 'декабря': '12'
    }

    day, month_name, year_time = date.split(' ', 2)
    year, time = year_time.split(', ')
    time = time.replace(' МСК', '')
    month = months[month_name]

    result = f"{time} {day}.{month}.{year}"
    return result

def parse_championat(url, nlp, classifier):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                      'Chrome/91.0.4472.124 Safari/537.36',
        'Accept-Language': 'ru-RU,ru;q=0.9'
    }

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')
        news_container = soup.find('div', class_='news-items')
        if not news_container:
            return []

        news_items = news_container.find_all('div', class_='news-item', limit=300)
        parsed_data = []

        for item in news_items:
            title_tag = item.find('a', class_='news-item__title')
            title = title_tag.get_text(strip=True) if title_tag else 'Без заголовка'
            link = urljoin(url, title_tag['href']) if title_tag else ''
            tags, date, author, text, ents, sentiment = parse_championat_tags(link, headers, nlp, classifier) if link else []
            tags = list(set(filter(None, tags)))

            date = format_date(date)

            parsed_data.append({
                'title': title,
                'link': link,
                'tags': tags,
                'date': date,
                'author': author,
                'text': text,
                'entities': ents,
                'sentiment': sentiment,
                'source': 'CHAMPIONAT'
            })

        return parsed_data

    except Exception as e:
        print(f"Ошибка: {str(e)}")
        return []