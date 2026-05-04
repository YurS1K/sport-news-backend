import time
from transformers import pipeline
from Parsers.ria_sport_parser import parse_ria_sport
import pandas as pd
import stanza

ria_urls = ["https://rsport.ria.ru/hockey/", "https://rsport.ria.ru/football/",
            "https://rsport.ria.ru/figure_skating/", "https://rsport.ria.ru/tennis/",
            "https://rsport.ria.ru/fights/", "https://rsport.ria.ru/lyzhnye-gonki/",
            "https://rsport.ria.ru/biathlon/", "https://rsport.ria.ru/category_formula_1/"]
match_url = "https://matchtv.ru/news"

data = []

stanza.download('ru')

nlp = stanza.Pipeline('ru',
                      processors='tokenize,pos,lemma,depparse,ner',
                      use_gpu=True)

classifier = pipeline(task="sentiment-analysis",
                      model="blanchefort/rubert-base-cased-sentiment",
                      truncation=True,
                      max_length=512)

for url in ria_urls:
    #time.sleep(30)
    for i in parse_ria_sport(url, nlp, classifier):
        data.append(i)

parsed = pd.DataFrame(data)
parsed.to_csv(f'D:\\projects\\sport\\src\main\\resources\\ria_parsed_data.csv', index=False, encoding="utf-8")
