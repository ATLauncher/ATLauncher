FROM alpine

RUN apk add gettext \
    && mkdir -p /deduplicateTranslations/out

WORKDIR /deduplicateTranslations

ADD in /deduplicateTranslations/in

RUN msguniq -o /deduplicateTranslations/out/translations.pot in/translations.pot
