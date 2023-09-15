# JSONEditor

allows editing JSON files to certain json schemas in a kind of more streamlined UI

supports basic JSON schemas and the unique keys vocabulary https://docs.json-everything.net/schema/vocabs/uniquekeys/

will maybe become a website/server at some point, javafx UI as a placeholder until then

workflow:

1. select schema
2. select file to edit (or skip)
3. try validating file through schema
4. if no, file cannot be edited, it has to match the format. New File and Schema must be chosen
5. if yes, file can be edited


you can configure extra buttons in the toolbar by writing a settings json and adding buttons like in the example settings file

