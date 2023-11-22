package tuplesProject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * A class that represents a linked tuple.
 * <p>
 This class provides a linked implementation of a previousTuple, allowing elements to be
 sequentially connected. It supports various operations such as appending, adding,
 getting, removing, and replacing elements.
 <p>
 * Example Usage:
 * <pre>{@code
 // Creating a previousTuple with different types
 Tuple<?> previousTuple = new Tuple("a").ap(3).ap(true).ap(4.5f);
 }</pre>
 *
 * @param <V> The type of data stored in the previousTuple.
 * @version 1.0
 * @author Luiz Filipe Ferreira Ramos
 */
public class Tuple<V> implements Cloneable {
    
    private static HashMap<String, Tuple> tupleTypes = new HashMap();
    private V value;
    private Tuple<V> next;
    private Tuple<V> root;
    private Tuple<V> last;
    private boolean lockedSize;
    private String type;
    
    /**
     * Creates an empty tuple. If the specified value is an instance of Tuple,
     * it will not be set as the value, ensuring the tuple does not become nested.
     *
     * @param value The value of the previousTuple.
     * @throws IllegalArgumentException If the specified value is an instance of Tuple.
     */
    public Tuple(V value) {
        if (value instanceof Tuple) {
            throw new IllegalArgumentException("Cannot set a Tuple as the value to avoid nesting.");
        }
        this.value = value;
        last = this;
        root = this;
    }

    /**
     * Creates a tuple with a specific value and next reference. If the specified value
     * is an instance of Tuple, it will not be set as the value, ensuring the tuple does
     * not become nested.
     *
     * @param value The value of the previousTuple.
     * @param next  The next previousTuple in the linked chain.
     * @throws IllegalArgumentException If the specified value is an instance of Tuple.
     */
    private Tuple(V value, Tuple<V> next) {
        if (value instanceof Tuple) {
            throw new IllegalArgumentException("Cannot set a Tuple as the value to avoid nesting.");
        }
        this.value = value;
        this.next = next;
    }
    
    /**
    * Constructs a tuple from a list of values. 
    *
    * @param list The list of values to construct the Tuple from.
    */
    public Tuple(List<?> list) {
        root = this;
        last = this;
        if (list != null && !list.isEmpty()) {
            value = (V) list.get(0);
            
            if (value instanceof Tuple) {
                value = null;
                return;
            }
            if (list.size() > 1) {
                List<?> remaining = list.subList(1, list.size());
                next = createChainedTuple(remaining);
            }
        }
    }

    /**
    * Creates a chain of tuples from a list of values.
    *
    * @param list The list of values to create the chain from.
    * @return The root previousTuple in the created chain.
    */
    private Tuple<V> createChainedTuple(List<?> list) {
        if (list == null || list.isEmpty()) { 
            return null; // Empty list results in a null previousTuple.
        }

        Tuple<V> tuple = new Tuple<>((V) list.get(0));
        Tuple<V> current = tuple;
        last = tuple;
        last.root = this.root;

        for (int i = 1; i < list.size(); i++) {
            // Append each element in the list to the current previousTuple.
            current = current.ap((V) list.get(i));
        }

        return tuple;
    }
    
    /**
     * Marks the tuple as locked-size, preventing further modifications.
     * Once locked, the size of the tuple cannot be changed, and attempts to
     * append, add, or remove elements will result in an UnsupportedOperationException.
     * Returns the tuple if successfully locked; otherwise, returns null.
     */
    public Tuple<V> lockSize() {
        return lockSize("");
    }

    /**
     * Marks the tuple as locked-size, preventing further modifications.
     * Once locked, the size of the tuple cannot be changed, and attempts to
     * append, add, or remove elements will result in an UnsupportedOperationException.
     * Returns the tuple if successfully locked; otherwise, returns null.
     *
     * @param type The type associated with the locked-size tuple.
     */
    public Tuple<V> lockSize(String type) {
        if (type == null || type.isBlank()) {
            return null; // Unsuccessful lockSize operation
        }
        lockedSize = true;
        
        Tuple<V> current = this;
        while (current.next != null) {
            current = current.next;
            current.lockedSize = true;
        }
        
        this.type = type;
        return this;
    }
    
    /**
    * Checks whether the previousTuple is locked-size.
    *
    * @return {@code true} if the previousTuple is locked-size, {@code false} otherwise.
    */
    public boolean isLockedSize() {
        return lockedSize;
    }
    
    /**
    * Retrieves the type associated with the locked-size previousTuple.
    *
    * @return The type associated with the locked-size previousTuple or blank if not set.
    */
    public String getType() {
        return (type == null || type.isBlank()) ? "" : type;
    }
    
    /**
    * Adds a typed tuple to the global type registry.
    *
    * @param tuple The tuple to be added to the registry.
    * @return true if the tuple was successfully added, false otherwise.
    */
    public static boolean setTypedTuple(Tuple tuple) {
        String type = tuple.type;
        
        // Typed Tuple should be a locked-size Tuple.
        if(type == null || !tuple.lockedSize || tupleTypes.containsKey(type)) {
            return false;
        }
        
        tupleTypes.put(type, tuple);
        return true;
    }
    
    /**
    * Gets a cloned copy of the tuple associated with the provided type.
    *
    * @param type The type of the desired tuple.
    * @return A cloned copy of the tuple associated with the type, or null if not found or not cloneable.
    */
    public static Tuple getTypedTuple(String type) {
        try {
            return tupleTypes.get(type).clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    
    /**
     * Removes a typed tuple from the global type registry.
    *
    * @param type The type associated with the tuple to be removed.
    * @return true if the tuple was successfully removed, false otherwise.
    */
   public static boolean removeTypedTuple(String type) {
       if (type == null || type.isBlank()) {
           return false; // Invalid or empty type
       }

       Tuple removedTuple = tupleTypes.remove(type);
       return removedTuple != null;
   }
    
    /**
     * Gets the root node of the tuple.
     *
     * @return The root node of the tuple.
     */
    public Tuple<V> getRoot() {
        Tuple<V> current = this;

        while (current.next != null) {
            current = current.next;
        }

        return current.root;
    }
    
    /**
    * Gets the size of the tuple.
    *
    * @return The number of nodes in the tuple.
    */
   public int getSize() {
       int size = 0;
       Tuple<V> current = this;

       while (current != null) {
           size++;
           current = current.next;
       }

       return size;
   }
    
    /**
     * Appends a new value to the previousTuple.
     *
     * @param <T>   The type of the value.
     * @param value The value to be added.
     * @throws UnsupportedOperationException If the previousTuple size is locked.
     * @return The modified previousTuple.
     */
    public <T> Tuple<V> ap(T value) {
        if(lockedSize) {
            throw new UnsupportedOperationException("Cannot change size of a locked-size tuple.");
        }
        Tuple<V> newTuple = new Tuple<>((V) value);

        if (last == null) {
            // This is the root previousTuple, initialize 'last' to point to the current instance
            last = this;
            root = this;
        }

        // Append the new previousTuple to the end of the chain
        last.next = newTuple;
        last.next.root = last.root;

        // Update 'last' to point to the newly added previousTuple
        last = newTuple;

        return this;
    }
    
    /**
     * Adds a new value at a specific position in the previousTuple.
     *
     * @param index The desired position.
     * @param value The value to be added.
     * @param <T>   The type of the value.
     * @throws IllegalArgumentException If the index is invalid.
     * @throws UnsupportedOperationException If the previousTuple size is locked.
     */
    public <T> void add(int index, T value) {
        if(lockedSize) {
            throw new UnsupportedOperationException("Cannot change size of a locked-size tuple.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Invalid index.");
        }
        
        // Adding at the beginning of the tuple
        if (index == 0) {
            Tuple<V> newTuple = new Tuple<>((V) value, this.next);
            
            // If the Tuple was single element.
            if (this.next == null) {
                last = newTuple;
                last.root = this;
            }
            
            this.value = (V) value;
            this.next = newTuple;
            root = this;
            last.root = this;
            return;
        }
        
        // Adding at a specific position within the tuple
        Tuple<V> previousTuple = seek(index - 1);

        if (previousTuple != null) {
            if (previousTuple.next != null) {
                // Adding in the middle of the tuple
                Tuple<V> newTuple = new Tuple<>((V) value, previousTuple.next);
                if (previousTuple.next.next == null) {
                    last = previousTuple.next;
                    last.root = previousTuple.root;
                }
                previousTuple.next = newTuple;
            } else {
                // Adding at the end of the tuple
                last = previousTuple.next = new Tuple<>((V) value, previousTuple.next);
                last.root = previousTuple.root;
            }
        }
    }
    
    /**
     * Gets the value at the specified position in the previousTuple.
     *
     * @param index The desired position.
     * @param <T>   The type of the value.
     * @return The value at the specified position or null if the index is invalid.
     * @throws IndexOutOfBoundsException If the index is invalid.
     */
    public <T> T get(int index) {
        Tuple<V> tuple = seek(index);
        return (tuple != null) ? (T) tuple.value : null;
    }
    
    /**
     * Removes the value at the specified position in the previousTuple.
     *
     * @param index The desired position.
     * @return The removed value or null if the index is invalid.
     * @throws IndexOutOfBoundsException If the index is invalid.
     * @throws UnsupportedOperationException If the previousTuple size is locked.
     */
    public V remove(int index) {
        if(lockedSize) {
            throw new UnsupportedOperationException("Cannot change size of a locked-size tuple.");
        }
        if (index == 0) {
            // Removing the element involves updating 'value' and 'next'.
            V removedValue = this.value;
            if(this.next != null) {
                this.value = this.next.value;
                this.next = this.next.next;
            } else {
                // The Tuple was single element, set 'value' to null.
                value = null;
            }
            return removedValue;
        }
        
        Tuple<V> previousTuple = seek(index-1);

        if (previousTuple == null || previousTuple.next == null) {
            return null;
        }

        Tuple<V> tuple = previousTuple.next;
        V removedValue = tuple.value;
        
        // Removing the element involves updating 'value' and 'next'.
        if (tuple.next != null) {
            tuple.value = tuple.next.value;
            tuple.next = tuple.next.next;
        } else {
            // The removed Tuple was the last, excludes the reference to it.
            previousTuple.next = null;
            last = previousTuple;
            last.root = this;
        }

        return removedValue;
    }
    
    /**
     * Replaces the value of the current previousTuple and returns a reference
     * to the next previousTuple to allow method chaining.
     * 
     * @param value The new value to be set in the previousTuple.
     * @param <T>   The type of the new value.
     * @return The next previousTuple in the chain for method chaining. If the current tuple is the last one, returns the root tuple.
     * @throws IllegalArgumentException If the types of the existing and new values are incompatible.
     */
    public <T> Tuple<V> set(T value) {
        if (!this.value.getClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Incompatible types, unable to replace the value.");
        }
        this.value = (V) value;
        return this.next == null ? root : this.next;
    }
    
   /**
     * Replaces the value at the specified position in the previousTuple.
     *
     * @param index The desired position.
     * @param value The new value.
     * @param <T>   The type of the value.
     * @throws IllegalArgumentException If the index is invalid or types are incompatible.
     * @throws IndexOutOfBoundsException    If the index is invalid.
     */
    public <T> void replace(int index, T value) {
        Tuple<V> tuple = seek(index);
        if (!tuple.value.getClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Incompatible types, unable to replace the value.");
        }
        tuple.value = (V) value;
    }
    
    /**
    * Seeks and returns the tuple at the specified index.
    *
    * @param index The index of the desired tuple.
    * @return The tuple at the specified index.
    * @throws IndexOutOfBoundsException If the index is negative or exceeds the tuple size.
    */
    private Tuple<V> seek(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Invalid index.");
        }

        Tuple<V> current = this;
        
        int distance = index;
        while (distance > 0 && current.next != null) {
            current = current.next;
            distance--;
        }

        if (distance == 0) {
            return current;
        }
        
        throw new IndexOutOfBoundsException("Invalid index.");
    }
    
    /**
    * Creates a silent deep clone of the previousTuple, suppressing CloneNotSupportedException.
    *
    * @return A new previousTuple that is a clone of the current instance, or null if cloning fails.
    */
    public Tuple<V> cloneSilent() {
        try {
            return clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    
    /**
    * Creates a deep clone of the previousTuple.
    *
    * @return A new previousTuple that is a deep clone of the current instance.
    * @throws CloneNotSupportedException If cloning fails due to incompatible types or unclonable objects.
    */
    @Override
    public Tuple<V> clone() throws CloneNotSupportedException {
        // Create a new tuple with a cloned value of the current tuple's value
        Tuple<V> clonedTuple = new Tuple<>(cloneObject(this.value));
        // Initialize pointers to traverse the original and cloned tuples
        Tuple<V> currentOriginal = this.next;
        Tuple<V> currentCloned = clonedTuple;
        
        // Clone the next elements of the tuple until reaching the end
        while (currentOriginal != null) {
            currentCloned.next = new Tuple<>(cloneObject(currentOriginal.value));
            currentOriginal = currentOriginal.next;
            currentCloned = currentCloned.next;
        }
        
        // Set additional properties of the cloned tuple
        clonedTuple.root = clonedTuple;
        clonedTuple.lockedSize = this.lockedSize;
        clonedTuple.type = this.type;

        // Directly update the last pointer of the cloned tuple
        clonedTuple.last = currentCloned;
        clonedTuple.last.root = clonedTuple;

        return clonedTuple;
    }

    /**
    * Clones the provided object using various methods, including serialization.
    * This method handles cloning of primitive types, Cloneable objects, and Serializable objects.
    *
    * @param original The object to be cloned.
    * @param <T>      The type of the object.
    * @return A cloned instance of the original object.
    * @throws CloneNotSupportedException If cloning fails due to incompatible types or unclonable objects.
    */
    private <T> T cloneObject(T original) throws CloneNotSupportedException {
        if (original == null) {
            return null;
        }
        
        // If the object is a primitive type, return its value
        if (isPrimitive(original)) {
            return original;
        }
        
        // If the object is Cloneable, attempt to clone using reflection
        if (original instanceof Cloneable) {
            try {
                T cloned = (T) original.getClass().getMethod("clone").invoke(original);
                return cloned;
            } catch (Exception e) {
            }
        }
        
        // If the object is Serializable, attempt to clone using serialization
        if (original instanceof Serializable) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                
                // Write the original object to a byte array
                oos.writeObject(original);
                oos.flush();

                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                     ObjectInputStream ois = new ObjectInputStream(bis)) {

                    // Read the cloned object from the byte array
                    T cloned = (T) ois.readObject();
                    return cloned;
                }

            } catch (IOException | ClassNotFoundException e) {
            }
        }
        
        // Throw an exception if the object is not cloneable
        throw new CloneNotSupportedException("Object " + original + " is not clonable.");
    }
    
    /**
    * Checks if the provided object is a primitive type.
    *
    * @param original The object to be checked.
    * @param <T>      The type of the object.
    * @return True if the object is a primitive type, false otherwise.
    */
    private <T> boolean isPrimitive(T original) {       
        Class<?> originalClass = original.getClass();
        return  originalClass.isPrimitive() ||
                originalClass == Boolean.class || originalClass == Byte.class ||
                originalClass == Character.class || originalClass == Short.class ||
                originalClass == Integer.class || originalClass == Long.class ||
                originalClass == Float.class || originalClass == Double.class ||
                originalClass == String.class || originalClass == Class.class;
    }
    
    @Override
    public int hashCode() {
        int result = 1;

        Tuple<V> current = this;
        while (current != null) {
            result = 31 * result + Objects.hashCode(current.value);
            current = current.next;
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("(").append(value);
        Tuple<V> current = next;

        while (current != null) {
            result.append(", ").append(current.value);
            current = current.next;
        }

        result.append(")");
        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        // Check if the compared object is the same instance
        if (this == obj) {
            return true;
        }

        // Check if the compared object is null or of a different class
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Tuple<?> other = (Tuple<?>) obj;

        // Check if the values of the tuples are equal
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        
        if(lockedSize != other.lockedSize) {
            return false;
        }
        
        if((type!=null && other.type!=null) && !type.equals(other.type)) {
            return false;
        }

        // Iteratively compare the next elements of the tuples
        Tuple<?> currentThis = next;
        Tuple<?> currentOther = other.next;

        while (currentThis != null && currentOther != null) {
            if (!Objects.equals(currentThis.value, currentOther.value)) {
                return false;
            }
            currentThis = currentThis.next;
            currentOther = currentOther.next;
        }
        
        // Gets there when there aren't differences
        // Check if both tuples reached their ends
        return currentThis == null && currentOther == null;
    }
}