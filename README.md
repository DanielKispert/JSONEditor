# JSONEditor

allows editing JSON files to certain json schemas in a kind of more streamlined UI

supports basic JSON schemas and the unique keys vocabulary https://docs.json-everything.net/schema/vocabs/uniquekeys/
also supports some object referencing keywords, see below.
It is used so the Editor can navigate between nodes with a single click.

will maybe become a website/server at some point, javafx UI as a placeholder until then

workflow:

1. select schema
2. select file to edit (or skip)
3. try validating file through schema
4. if no, file cannot be edited, it has to match the format. New File and Schema must be chosen
5. if yes, file can be edited


you can configure extra buttons in the toolbar by writing a settings json and adding buttons like in the example settings file

# How to use "referenceToObject"

at the root of the schema, add a "referencesToObjects" array like this:

    "referencesToObjects": [
        {
            "path": "/persons/*/references",
            "objectReferencingKey": "/type",
            "objectKey": "/identifier"
        }
    ]

at the root of the schema, add an array of referenceable objects like this:

    "referenceableObjects": [
        {
            "referencingKey": "hobby",
            "path": "/hobbies",
            "key": "/identifier"
        }
    ]

you should only use text or numbers for key nodes for now

# Templates

you can import existing JSON documents into any point of your json tree (given that the schema supports it).
You can also use a special syntax to mark variables in a json document and the editor will help you fill them out!
e.g. if you have a person object with name: <personname> and then somewhere else you would like to enter that as well then you can use <personname> again.
Any string in <> will be assumed to be a variable and can be filled out by the json editor. That allows you to do predetermined mass-replacement!

# Icons

This project is using the google material icons (https://fonts.google.com/icons?icon.set=Material+Icons), which are licensed under the Apache License.
You can find the license in the resources folder or in their Github repository (https://github.com/google/material-design-icons/blob/master/LICENSE).

## Column Visibility
Empty non-required columns in array tables auto-hide (default ON). Click the eye (left of table name) to show all columns until the table refreshes; click again to hide. If you type a value into a previously empty column it stays visible.
