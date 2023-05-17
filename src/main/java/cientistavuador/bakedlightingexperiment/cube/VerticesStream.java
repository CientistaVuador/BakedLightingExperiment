/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.bakedlightingexperiment.cube;

import java.util.Arrays;

/**
 *
 * @author Cien
 */
public class VerticesStream {
    
    private float[] vertices = new float[64];
    private int verticesIndex = 0;

    private int[] indices = new int[64];
    private int indicesIndex = 0;

    private int offset = 0;

    private final int textureWidth;
    private final int textureHeight;
    private final int lightmapWidth;
    private final int lightmapHeight;
    
    private final float texScaleX;
    private final float texScaleY;
    private final float mapScaleX;
    private final float mapScaleY;

    public VerticesStream(int textureWidth, int textureHeight, int lightmapWidth, int lightmapHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.lightmapWidth = lightmapWidth;
        this.lightmapHeight = lightmapHeight;
        
        this.texScaleX = textureWidth / 512f;
        this.texScaleY = textureHeight / 512f;
        this.mapScaleX = lightmapWidth / 512f;
        this.mapScaleY = lightmapHeight / 512f;
    }

    public VerticesStream() {
        this(512, 512, 512, 512);
    }
    
    public int numberOfVertices() {
        return this.verticesIndex / Cube.VERTEX_SIZE_ELEMENTS;
    }
    
    public int numberOfIndices() {
        return this.indicesIndex;
    }

    private float floor(float x) {
        return (float) Math.floor(x);
    }
    
    public void vertex(float x, float y, float z, float nX, float nY, float nZ, float texX, float texY, float cornerX, float cornerY) {
        if ((this.verticesIndex + Cube.VERTEX_SIZE_ELEMENTS) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + Cube.VERTEX_SIZE_ELEMENTS);
        }
        
        this.vertices[this.verticesIndex + 0] = x;
        this.vertices[this.verticesIndex + 1] = y;
        this.vertices[this.verticesIndex + 2] = z;
        this.vertices[this.verticesIndex + 3] = nX;
        this.vertices[this.verticesIndex + 4] = nY;
        this.vertices[this.verticesIndex + 5] = nZ;
        
        float colorX = ((floor(texX * this.texScaleX) + ((floor(this.texScaleX) - 1) * cornerX)) + 0.5f) / this.textureWidth;
        float colorY = ((floor(texY * this.texScaleY) + ((floor(this.texScaleY) - 1) * cornerY)) + 0.5f) / this.textureHeight;
        float mapX = ((floor(texX * this.mapScaleX) + ((floor(this.mapScaleX) - 1) * cornerX)) + 0.5f) / this.lightmapWidth;
        float mapY = ((floor(texY * this.mapScaleY) + ((floor(this.mapScaleY) - 1) * cornerY)) + 0.5f) / this.lightmapHeight;
        
        this.vertices[this.verticesIndex + 6] = colorX;
        this.vertices[this.verticesIndex + 7] = colorY;
        this.vertices[this.verticesIndex + 8] = mapX;
        this.vertices[this.verticesIndex + 9] = mapY;
        
        float mapPosX = mapX + ((0.5f / this.lightmapWidth) * ((cornerX - 0.5f) * 2f));
        float mapPosY = mapY + ((0.5f / this.lightmapHeight) * ((cornerY - 0.5f) * 2f));
        
        this.vertices[this.verticesIndex + 10] = (mapPosX * 2f) - 1f;
        this.vertices[this.verticesIndex + 11] = (mapPosY * 2f) - 1f;
        
        this.verticesIndex += Cube.VERTEX_SIZE_ELEMENTS;
    }
    
    public void index(int index) {
        if (this.indices.length == this.indicesIndex) {
            this.indices = Arrays.copyOf(this.indices, this.indices.length * 2);
        }
        this.indices[this.indicesIndex] = index + this.offset;
        this.indicesIndex++;
    }

    public void index(int[] indices) {
        if ((this.indicesIndex + indices.length) > this.indices.length) {
            this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + indices.length);
        }
        for (int i = 0; i < indices.length; i++) {
            this.indices[this.indicesIndex] = indices[i] + this.offset;
            this.indicesIndex++;
        }
    }

    public void triangle(int i0, int i1, int i2) {
        if ((this.indicesIndex + 3) > this.indices.length) {
            this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + 3);
        }

        this.indices[this.indicesIndex + 0] = i0 + this.offset;
        this.indices[this.indicesIndex + 1] = i1 + this.offset;
        this.indices[this.indicesIndex + 2] = i2 + this.offset;

        this.indicesIndex += 3;
    }

    public void quad(int a0, int a1, int a2, int b0, int b1, int b2) {
        if ((this.indicesIndex + 6) > this.indices.length) {
            this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + 6);
        }

        this.indices[this.indicesIndex + 0] = a0 + this.offset;
        this.indices[this.indicesIndex + 1] = a1 + this.offset;
        this.indices[this.indicesIndex + 2] = a2 + this.offset;
        this.indices[this.indicesIndex + 3] = b0 + this.offset;
        this.indices[this.indicesIndex + 4] = b1 + this.offset;
        this.indices[this.indicesIndex + 5] = b2 + this.offset;

        this.indicesIndex += 6;
    }

    public void offset() {
        this.offset = (this.verticesIndex / Cube.VERTEX_SIZE_ELEMENTS);
    }

    public float[] vertices() {
        return Arrays.copyOf(this.vertices, this.verticesIndex);
    }

    public int[] indices() {
        return Arrays.copyOf(this.indices, this.indicesIndex);
    }

}
