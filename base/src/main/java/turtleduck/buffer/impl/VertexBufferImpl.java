package turtleduck.buffer.impl;

import java.nio.ByteBuffer;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import turtleduck.buffer.VertexAttribute;
import turtleduck.buffer.CustomVertexBuffer;
import turtleduck.buffer.VertexBuffer;
import turtleduck.colors.Color;
import turtleduck.geometry.DirectionVector;
import turtleduck.geometry.PositionVector;

public class VertexBufferImpl<T extends CustomVertexBuffer.Base<T>> implements CustomVertexBuffer.Default<T>, CustomVertexBuffer.WithTexture4<T> { //VertexBuffer.Base<T>, VertexBuffer.WithColor<T>,
static class DefaultVertexBufferImpl extends VertexBufferImpl<VertexBuffer> implements VertexBuffer {
    
}
   VertexAttribute<Color> colorAttr;
   VertexAttribute<Color> posAttr;
   VertexAttribute<Color> normAttr;
   ByteBuffer target;
   ByteBuffer buffer;
   
    @Override
    public T color(Color col) {
        colorAttr.write(target, col);
        return (T)this;
    }

    @Override
    public T colorRGBA(int col) {
        colorAttr.write(target, col);
        return (T)this;
    }

    @Override
    public T colorBGRA(int col) {
        colorAttr.write(target, col);
     return (T)this;
    }

    @Override
    public T colorRGBA(int r, int g, int b, int a) {
        colorAttr.write(target, r, g, b, a);
        return (T)this;
    }

    @Override
    public T color(Vector3fc rgb) {
        colorAttr.write(target, rgb.x(), rgb.y(), rgb.z(), 1);
        return (T)this;
    }

    @Override
    public T color(Vector3fc rgb, double a) {
        colorAttr.write(target, rgb.x(), rgb.y(), rgb.z(), (float)a);
        return (T)this;
    }

    @Override
    public T color(Vector4fc rgba) {
        colorAttr.write(target, rgba.x(), rgba.y(), rgba.z(), rgba.w());
        return (T)this;
    }

    @Override
    public T color(double r, double g) {
        colorAttr.write(target, (float)r, (float)g, 0f, 1f);
        return (T)this;
    }

    @Override
    public T color(double r, double g, double b) {
        colorAttr.write(target, (float)r, (float)g, (float)b, 1f);
        return (T)this;
    }

    @Override
    public T color(double r, double g, double b, double a) {
        colorAttr.write(target, (float)r, (float)g, (float)b, (float)a);
        return (T)this;
    }

    @Override
    public T next() {
        target.rewind();
        buffer.put(target);
        target.rewind();
        return (T)this;
    }

    @Override
    public <U> T set(VertexAttribute<U> attr, U value) {
        attr.write(target, value);
        return (T)this;
    }

    @Override
    public T set(int location, Color col) {
        return (T)this;
    }

    @Override
    public T set(int location, PositionVector pos) {
        return (T)this;
    }

    @Override
    public T set(int location, DirectionVector dir) {
        return (T)this;
    }

    @Override
    public T set(int location, Vector2fc xy) {
        return (T)this;
    }

    @Override
    public T set(int location, Vector2fc xy, double z) {
        return (T)this;
    }

    @Override
    public T set(int location, Vector2fc xy, double z, double w) {
        return (T)this;
    }

    @Override
    public T set(int location, Vector3fc xyz) {
        return (T)this;
    }

    @Override
    public T set(int location, Vector3fc xyz, double w) {
        return (T)this;
    }

    @Override
    public T set(int location, Vector4fc xyzw) {
        return (T)this;
    }

    @Override
    public T set(int location, double x) {
        return (T)this;
    }

    @Override
    public T set(int location, double x, double y) {
        return (T)this;
    }

    @Override
    public T set(int location, double x, double y, double z) {
        return (T)this;
    }

    @Override
    public T set(int location, double x, double y, double z, double w) {
        return (T)this;
    }

    @Override
    public T texcoord(Vector3fc stp) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(Vector3fc stp, double q) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(Vector4fc stpq) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(double s, double t, double p) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(double s, double t, double p, double q) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(PositionVector pos) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(Vector2fc st) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T texcoord(double s, double t) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T pointSize(double pointSize) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(DirectionVector nrm) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(Vector2fc xy) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(Vector2fc xy, double z) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(Vector2fc xy, double z, double w) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(Vector3fc xyz) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(Vector3fc xyz, double w) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(Vector4fc xyz) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(double x, double y) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(double x, double y, double z) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T normal(double x, double y, double z, double w) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(PositionVector pos) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(Vector2fc xy) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(Vector2fc xy, double z) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(Vector2fc xy, double z, double w) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(Vector3fc xyz) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(Vector3fc xyz, double w) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(Vector4fc xyzw) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(double x, double y) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(double x, double y, double z) {
        // TODO Auto-generated method stub
        return (T)this;
    }

    @Override
    public T position(double x, double y, double z, double w) {
        // TODO Auto-generated method stub
        return (T)this;
    }

}
