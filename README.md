# jsong
## json generator

## Sample Program
`Jsong jsong = new Jsong(File file);`
`String jsonString1 = jsong.generate("001");`
`String jsonString2 = jsong.generate("002");`

## Sample Csv File

|キー|||型|001|002|
|---|---|---|---|---|---|
|obj1|||{}|||
||prop1||String|val1|val2|
||prop2||Number|22|33|
||prop3||Boolean|true|false|
||obj2||{}|||
|||prop4|String|val3|va4|
|||prop5|Number|3|4|
|||prop6|Boolean|true|false|


##sample config file
conf/jsong.properties
key=キー
type=型

keyで指定した列から右へ探索して列名が空の列までをkey列と判定する。

