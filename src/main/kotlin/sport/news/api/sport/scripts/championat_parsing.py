import time
from Parsers.championat_parser import parse_championat
import pandas as pd
from Parsers.model_loader import get_nlp, get_classifier

data = []

nlp = get_nlp()
classifier = get_classifier()

for page in range(1, 2):
    time.sleep(30)
    print(f"Parsing https://www.championat.com/news/{page}.html")
    for i in parse_championat(f"https://www.championat.com/news/{page}.html", nlp, classifier):
        data.append(i)

parsed = pd.DataFrame(data)
parsed.to_csv(f'D:\\projects\\sport\\src\\main\\resources\\championat_parsed_data.csv', index=False, encoding="utf-8")