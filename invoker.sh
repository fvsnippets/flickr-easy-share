#!/bin/bash
mvn compile exec:java -Dexec.mainClass="org.github.fvsnippets.flickr_easy_html_share.App" -Dexec.args="${HOME}/.flickr-easy-share"
