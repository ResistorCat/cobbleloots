package dev.ripio.cobbleloots.data.custom;

import dev.ripio.cobbleloots.Cobbleloots;
import net.minecraft.resources.ResourceLocation;

public class CobblelootsLootBallResourceLocation {
    private final String namespace;
    private final String path;
    private final String variant;

    public CobblelootsLootBallResourceLocation(String locationString) {
        String[] parts = locationString.split(":");
        switch (parts.length) {
            case 1:
                // "poke" -> cobbleloots:poke:*
                this.namespace = "cobbleloots";
                this.path = parts[0];
                this.variant = "*";
                break;
            case 2:
                if (parts[1].equals("*")) {
                    // "custompack:*" -> custompack:*:*
                    this.namespace = parts[0];
                    this.path = "*";
                    this.variant = "*";
                } else if (ResourceLocation.isValidNamespace(parts[0])) {
                    // If the first part is a valid namespace, assume "namespace:path"
                    this.namespace = parts[0];
                    this.path = parts[1];
                    this.variant = "*";
                } else {
                    // Otherwise, assume "path:variant"
                    this.namespace = "cobbleloots";
                    this.path = parts[0];
                    this.variant = parts[1];
                }
                break;
            case 3:
                // "custompack:poke:extra" -> custompack:poke:extra
                // "cobbleloots:poke:*" -> cobbleloots:poke:*
                this.namespace = parts[0];
                this.path = parts[1];
                this.variant = parts[2];
                break;
            default:
                throw new IllegalArgumentException("Invalid loot ball location format: " + locationString);
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public String getVariant() {
        return variant;
    }

    public ResourceLocation getResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public boolean matches(String namespace, String path, String variant) {
        boolean namespaceMatches = this.namespace.equals("*") || this.namespace.equals(namespace);
        boolean pathMatches = this.path.equals("*") || this.path.equals(path);
        boolean variantMatches = this.variant.equals("*") || this.variant.equals(variant);

        if (this.path.equals("*")) {
            return namespaceMatches;
        }
        if (this.variant.equals("*")) {
            return namespaceMatches && pathMatches;
        }

        return namespaceMatches && pathMatches && variantMatches;
    }

    @Override
    public String toString() {
        return namespace + ":" + path + ":" + variant;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CobblelootsLootBallResourceLocation that = (CobblelootsLootBallResourceLocation) obj;
        return namespace.equals(that.namespace) && path.equals(that.path) && variant.equals(that.variant);
    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + variant.hashCode();
        return result;
    }
}
