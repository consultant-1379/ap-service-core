#!/bin/sh

if ! [ -x "$(command -v npm)" ]; then
    echo "Unable to convert RAML to HTML. NPM is required."
else
    if ! [ -x "$(command -v raml2html)" ]; then
        echo "----------------------------------------------"
        echo "------------ Installing raml2html ------------"
        echo "----------------------------------------------"
        sudo npm i -g raml2html
    fi

    if [ -x "$(command -v raml2html)" ]; then
        echo "\nConverting RAML to HTML, please standby ..."
        raml2html ap-core-rest-war.raml > ../../src/site/resources/ap-core-rest-war.html

        echo "\n-------------------------------------------------------------------------------------------------------------------------"
        echo "----- Conversion completed. Generated HTML file copied to /ap-service-core/src/site/resources/ap-core-rest-war.html -----"
        echo "-------------------------------------------------------------------------------------------------------------------------"
    else
        echo "Error: Something went wrong. Unable to convert raml2html."
    fi
fi

bash

