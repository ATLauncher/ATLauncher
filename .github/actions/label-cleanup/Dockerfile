FROM node:16-alpine

WORKDIR /action

COPY package.json /action/package.json
RUN npm install

COPY . /action

ENTRYPOINT [ "node", "/action/action.js" ]
