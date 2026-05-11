import time
from Parsers.ria_sport_parser import parse_ria_sport
import pandas as pd
from Parsers.model_loader import get_nlp, get_classifier

ria_urls = ["https://rsport.ria.ru/hockey/", "https://rsport.ria.ru/football/",
            "https://rsport.ria.ru/figure_skating/", "https://rsport.ria.ru/tennis/",
            "https://rsport.ria.ru/fights/", "https://rsport.ria.ru/lyzhnye-gonki/",
            "https://rsport.ria.ru/biathlon/", "https://rsport.ria.ru/category_formula_1/"]

data = []

nlp = get_nlp()
classifier = get_classifier()

for url in ria_urls:
    for i in parse_ria_sport(url, nlp, classifier):
        data.append(i)

parsed = pd.DataFrame(data)
parsed.to_csv(f'D:\\projects\\sport\\src\\main\\resources\\ria_parsed_data.csv', index=False, encoding="utf-8")