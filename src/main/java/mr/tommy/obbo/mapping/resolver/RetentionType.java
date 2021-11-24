package mr.tommy.obbo.mapping.resolver;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the Retention type of the providers.
 */
public enum RetentionType {
    /**
     * Value is checked everytime it wants it to be resolved.
     */
    @SerializedName("lazy") LAZY,
    /**
     * Value is stored the first time is checked then the same
     * value is going to be sent every time the Provider is going
     * to resolve.
     */
    @SerializedName("cached") CACHED
}
