package turtleduck.buffer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import turtleduck.buffer.VertexAttribute;
import turtleduck.buffer.VertexLayout;
import turtleduck.buffer.impl.VertexAttributeImpl.DataField1f;
import turtleduck.buffer.impl.VertexAttributeImpl.DataField1i;
import turtleduck.buffer.impl.VertexAttributeImpl.DataField2f;
import turtleduck.buffer.impl.VertexAttributeImpl.DataField3f;
import turtleduck.buffer.impl.VertexAttributeImpl.DataField4c;
import turtleduck.buffer.impl.VertexAttributeImpl.DataField4f;
import turtleduck.colors.Color;

public class VertexLayoutImpl implements VertexLayout, VertexLayout.LayoutBuilder, VertexLayout.InputFormatBuilder {
    public static final int GL_BYTE = 0x1400, GL_UNSIGNED_BYTE = 0x1401, GL_SHORT = 0x1402, GL_UNSIGNED_SHORT = 0x1403,
            GL_INT = 0x1404, GL_UNSIGNED_INT = 0x1405, GL_FLOAT = 0x1406, GL_DOUBLE = 0x140A, //
            GL_UNSIGNED_INT_2_10_10_10_REV = 0x0000, GL_SIGNED_INT_2_10_10_10_REV = 0x0000;

    protected Map<String, VertexAttributeImpl<?>> names = new HashMap<>();
    protected List<VertexAttributeImpl<?>> fields = new ArrayList<>();
    protected List<Type> types = new ArrayList<>();
    protected int vertexSize = 0;
    protected int location = 0;
    protected boolean DEBUG;

    public VertexLayoutImpl() {
    }

    protected void addield(VertexAttribute<?> field) {

//		Collections.sort(fields, (f1,f2) -> Integer.compare(f1.location, f2.location));
    }

    @SuppressWarnings("unchecked")
    protected <T> VertexAttributeImpl<T> makeField(String name, Class<T> type, int loc, int off, int index) {
        if (type == Integer.class) {
            return (VertexAttributeImpl<T>) new VertexAttributeImpl.DataField1i(name, loc, off, index);
        } else if (type == Float.class) {
            return (VertexAttributeImpl<T>) new VertexAttributeImpl.DataField1f(name, loc, off, index);
        } else if (Vector2fc.class.isAssignableFrom(type)) {
            return (VertexAttributeImpl<T>) new VertexAttributeImpl.DataField2f(name, loc, off, index);
        } else if (Vector3fc.class.isAssignableFrom(type)) {
            return (VertexAttributeImpl<T>) new VertexAttributeImpl.DataField3f(name, loc, off, index);
        } else if (Vector4fc.class.isAssignableFrom(type)) {
            return (VertexAttributeImpl<T>) new VertexAttributeImpl.DataField4f(name, loc, off, index);
        } else if (Color.class.isAssignableFrom(type)) {
//			return (DataField<T>) new DataField.DataField4cf(name, loc, off);
            return (VertexAttributeImpl<T>) new VertexAttributeImpl.DataField4c(name, loc, off, index);
        } else {
            throw new IllegalArgumentException(type.getName());
        }
    }

    public <T> VertexLayout.LayoutBuilder declare(String name, Class<T> type) {
        int index = fields.size();
        VertexAttributeImpl<T> field = makeField(name, type, location, vertexSize, index);
        names.put(name, field);
        types.add(field.type);
        fields.add(field);
        location += field.numLocations();
        vertexSize += field.numBytes();
        return this;
    }
    public <T> VertexLayout.LayoutBuilder declare(String name, int loc, Class<T> type) {
        int index = fields.size();
        VertexAttributeImpl<T> field = makeField(name, type, loc, vertexSize, index);
        names.put(name, field);
        types.add(field.type);
        fields.add(field);
        location = Math.max(location, loc);
        location += field.numLocations();
        vertexSize += field.numBytes();
        return this;
    }
    public <T> VertexLayout.LayoutBuilder declare(String name, String role, Class<T> type) {
        int index = fields.size();
        VertexAttributeImpl<T> field = makeField(name, type, location, vertexSize, index);
        names.put(role, field);
        types.add(field.type);
        fields.add(field);
        location += field.numLocations();
        vertexSize += field.numBytes();
        return this;
    }

    public VertexLayout.InputFormatBuilder setInputFormat(String name, Type type) {
        VertexAttributeImpl<?> oldField = names.get(name);

        types.set(oldField.index, type);
        return this;
    }

    public String toString() {
        return fields.toString();
    }

    public <T> VertexAttribute<T> attribute(int index) {
        return (VertexAttribute<T>) fields.get(index);
    }

    public <T> VertexAttribute<T> attribute(String name) {
        return (VertexAttribute<T>) names.get(name);
    }
    public <T>  VertexAttribute<T> attribute(String name, Class<T> type) {
        return (VertexAttribute<T>) names.get(name);
    }
    public Type inputFormat(String roleOrName) {
        return types.get(names.get(roleOrName).index);
    }

    public Type inputFormat(int index) {
        return types.get(index);
    }

    @Override
    public int numBytes() {
        return vertexSize;
    }

    @Override
    public int numAttributes() {
        return fields.size();
    }

    @Override
    public int numLocations() {
        return location;
    }

    @Override
    public VertexLayout done() {
        fields = Collections.unmodifiableList(fields);
        types = Collections.unmodifiableList(types);
        names = Collections.unmodifiableMap(names);
        return this;
    }

    @Override
    public InputFormatBuilder specifyInputFormat() {
        VertexLayoutImpl other = new VertexLayoutImpl();
        other.fields = this.fields;
        other.types = new ArrayList<>(this.types);
        other.location = this.location;
        other.names = this.names;
        other.vertexSize = this.vertexSize;

        return other;
    }
}