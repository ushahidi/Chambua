#Chambua
Chambua is an open-source semantic tagging application. By exposing a simple REST API, it provides a convenient way to analyse text and extract words/terms that can be classified as people, places & organisations. It can also recognise nationalities, religions, expressions of time and monetary values.

The semantic extraction layer is powerd by [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) natural language analysis toolset

## Installation
To install Chambua on your system, the following packages must be installed:

* [Java 1.6+](http://www.oracle.com/technetwork/java/index.html)
* [Apache Maven](http://maven.apache.org)


### 1. Download the Stanford NER Models
Stanford CoreNLP provides a set of pre-trained classifiers. These can be downloaded from [here](http://nlp.stanford.edu/software/CRF-NER.shtml). The trained classifiers can be found in the ``classifiers`` directory of the extracted archive

### 2. Checkout the code from GitHub

	git checkout git://github.com/ushahidi/Chambua.git

### 3. Configure chambua.properties

* #### Create ``$CHAMBUA_HOME``
	
		mkdir -p /etc/chambua
		export CHAMBUA_HOME=/etc/chambua

* #### Create ``chambua.properties``
	
		cp src/main/resources/chambua.properties.sample /etc/chambua/chambua.properties

	Open ``chambua.properties`` and point ``chambua.classifier.dir`` to the directory with 	Stanford CoreNLP classifers extracted from the archive you dowloaded in Step (1) above.

### Build and Deploy

	mvn tomcat7:run-war


## Using the REST API
Chambua exposes a single endpoint - ``/v1/tags`` - that accepts HTTP POST requests.

	curl -H "Content-Type:application/json" -X POST -d @data/sample_001.txt http://localhost:8080/chambua/v1/tags

## Authors
* Emmanuel Kala ([@bytebandit](https://twitter.com/bytebandit))

