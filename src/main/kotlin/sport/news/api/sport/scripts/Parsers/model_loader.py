# Parsers/model_loader.py
import stanza
from transformers import pipeline
import threading
import os
import time
import random

_nlp = None
_classifier = None
_model_lock = threading.Lock()

def init_models():
    """Инициализация моделей с задержкой для предотвращения конфликтов"""
    global _nlp, _classifier

    with _model_lock:
        if _nlp is not None:
            return _nlp, _classifier

        delay = random.uniform(0.5, 3.0)
        print(f"Ожидание {delay:.1f} секунд перед загрузкой моделей...")
        time.sleep(delay)

        if _nlp is not None:
            return _nlp, _classifier

        print("Загрузка моделей stanza...")

        os.environ['STANZA_RESOURCES_DIR'] = os.path.expanduser("~/stanza_resources")

        resources_dir = os.path.expanduser("~/stanza_resources")
        if not os.path.exists(resources_dir):
            try:
                stanza.download('ru')
            except Exception as e:
                print(f"Ошибка при загрузке stanza: {e}")
                pass

        _nlp = stanza.Pipeline('ru',
                               processors='tokenize,pos,lemma,depparse,ner',
                               use_gpu=True,
                               verbose=False)
        print("Модель stanza загружена")

        print("Загрузка модели sentiment analysis...")
        _classifier = pipeline(task="sentiment-analysis",
                               model="blanchefort/rubert-base-cased-sentiment",
                               truncation=True,
                               max_length=512,
                               device=0)
        print("Модель sentiment analysis загружена")

        return _nlp, _classifier

def get_nlp():
    global _nlp
    if _nlp is None:
        init_models()
    return _nlp

def get_classifier():
    global _classifier
    if _classifier is None:
        init_models()
    return _classifier