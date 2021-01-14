FROM alpine

RUN apk add gettext \
    && mkdir -p /processTranslations/out \
    && mkdir -p /processTranslations/tmp

WORKDIR /processTranslations

ADD convert.sh /processTranslations
ADD in /processTranslations/in

RUN chmod +x convert.sh

RUN ./convert.sh
