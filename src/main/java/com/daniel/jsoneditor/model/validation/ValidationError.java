package com.daniel.jsoneditor.model.validation;


public final class ValidationError
{
    public enum Type
    {
        EMPTY_KEY,
        DANGLING_REFERENCE
    }

    private final String path;
    private final Type type;
    private final String referencingKey;
    private final String objectKey;
    private final String referencedObjectPath;

    private ValidationError(final String path, final Type type, final String referencingKey, final String objectKey,
        final String referencedObjectPath)
    {
        this.path = path;
        this.type = type;
        this.referencingKey = referencingKey;
        this.objectKey = objectKey;
        this.referencedObjectPath = referencedObjectPath;
    }

    /**
     * Create a ValidationError for an empty key at the given JSON pointer path.
     */
    public static ValidationError emptyKey(final String path)
    {
        return new ValidationError(path, Type.EMPTY_KEY, null, null, null);
    }

    /**
     * Create a ValidationError for a dangling reference.
     *
     * @param path raw JSON pointer path where the error occurred
     * @param referencingKey the schema key type referencing the object (e.g. "item_ref")
     * @param objectKey the unresolved object key value
     * @param referencedObjectPath the raw path of the referenced object array
     */
    public static ValidationError danglingReference(final String path, final String referencingKey, final String objectKey,
        final String referencedObjectPath)
    {
        return new ValidationError(path, Type.DANGLING_REFERENCE, referencingKey, objectKey, referencedObjectPath);
    }

    /**
     * Raw JSON pointer path of the error location.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Type of validation error.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * For DANGLING_REFERENCE: the schema key type referencing the object; null for EMPTY_KEY.
     */
    public String getReferencingKey()
    {
        return referencingKey;
    }

    /**
     * For DANGLING_REFERENCE: the unresolved object key; null for EMPTY_KEY.
     */
    public String getObjectKey()
    {
        return objectKey;
    }

    /**
     * For DANGLING_REFERENCE: the raw path of the object array; null for EMPTY_KEY.
     */
    public String getReferencedObjectPath()
    {
        return referencedObjectPath;
    }

    @Override
    public String toString()
    {
        return "ValidationError{type=" + type + ", path='" + path + "'}";
    }
}
