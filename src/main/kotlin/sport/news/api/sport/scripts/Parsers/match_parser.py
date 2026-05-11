import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin
from transformers import pipeline


def parse_match_author(article_url, headers):
    try:
        response = requests.get(article_url, headers=headers)
        response.raise_for_status()
        article_soup = BeautifulSoup(response.text, 'html.parser')


        author_tag = article_soup.find('span', class_="typography typography--level-text-2 color color--text-color--basic-black")
        author = author_tag.get_text(strip=True) if author_tag else "Нет автора"

        parags = [parag.get_text(strip=False) for parag in
                  article_soup.find('div', class_='p-news-details-body-html').find_all('p')]
        text = " ".join(parags)

        print(text)
        return author, text

    except Exception as e:
        print(f"Ошибка при парсинге статьи: {str(e)}")
        return []


def parse_match(url):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Language': 'ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7',
        'Accept-Encoding': 'gzip, deflate, br',
        'DNT': '1',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'none',
        'Sec-Fetch-User': '?1',
        'Cache-Control': 'max-age=0',
    }

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')

        news_container = soup.find('div', class_='flex flex--display-block flex--column-20 flex--row-20 flex--direction-vertical')
        if not news_container:
            return []

        news_items = news_container.find_all('div', class_='flex flex--display-block flex--column-8 flex--row-8 flex--direction-vertical m-media-card-wrapper m-news-feed-media-card p-news-list-item', limit=100)

        parsed_data = []
        for item in news_items:
            title = item.find('div', class_='m-media-card__title').get_text()

            tags = item.find('span',
                             class_='typography typography--level-text-2 typography--modifier-caps a-badge__text color color--text-color--gray-600 a-rubric-badge__text').get_text()
            if tags == 'ТЕЛЕНОВОСТИ':
                continue
            link = urljoin(url, item.find('a', class_='hover a-link-wrapper a-link-wrapper--display-block a-link-wrapper--reset-styles m-media-card__title--wrapped-by-link')['href'])
            print(link)
            date_tags = item.find('time', class_='m-media-card-date m-news-feed-media-card__date')
            date = date_tags.get_text()



            author, text = parse_match_author(link, headers) if link else []

            date = date[0:18]

            parsed_data.append({
                'title': title,
                'link': link,
                'tags': tags,
                'date': date,
                'author': author,
                'text': text
            })

        return parsed_data

    except Exception as e:
        print(f"Ошибка: {str(e)}")
        return []

