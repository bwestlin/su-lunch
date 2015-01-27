# su-lunch #
> Dagens luncher - Stockholms Universitet

This is a simplistic web-application used for aggregating lunch menus in the area around Stockholm University and it's currently alive at **http://su-lunch.herokuapp.com/**.

The application is built using **[Play Framework](http://www.playframework.com)** and written mainly in **[Scala](http://www.scala-lang.org/)**.
Data collecting is done with **[Web scraping](http://en.wikipedia.org/wiki/Web_scraping)** using the **[jsoup](http://jsoup.org/)** HTML Parser.

## Running ##
The easiest way to run this application is with the following command in the project directory:
```
./activator run
```
After this the application can be accessed from a web browser at **http://localhost:9000/** however it might take some time initially for the dependencys to resolve.

## IDE setup for **[IntelliJ IDEA](http://www.jetbrains.com/idea/)**##
Either import the project as an SBT project in IDEA

or

issue the following command in the project directory:
```
./activator idea
```
This creates an IntelliJ project which can be opened from IntelliJ as a regular project.

To run the application during development and make it recompile directly on source-code changes start it with the following command:
```
./activator ~run
```

## Licence ##

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Copyright &copy; 2013- Björn Westlin.

