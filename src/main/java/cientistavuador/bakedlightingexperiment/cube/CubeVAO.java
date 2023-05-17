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

import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class CubeVAO {
    
    public static final int GROUND_CUBE_VAO;
    public static final int GROUND_CUBE_WIDTH = 4096;
    public static final int GROUND_CUBE_HEIGHT = 4096;
    
    public static final int VAO;
    private static int vbo;
    private static int lightmapWidth;
    private static int lightmapHeight;

    public static int getLightmapHeight() {
        return lightmapHeight;
    }

    public static int getLightmapWidth() {
        return lightmapWidth;
    }
    
    static {
        GROUND_CUBE_VAO = generateVao(GROUND_CUBE_WIDTH, GROUND_CUBE_HEIGHT);
        VAO = generateVao(512, 512);
    }
    
    private static int generateVao(int width, int height) {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        VerticesStream stream = generateVertices(width, height);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, stream.indices(), GL_STATIC_DRAW);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, stream.vertices(), GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, Cube.VERTEX_SIZE_ELEMENTS * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Cube.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3) * Float.BYTES);

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Cube.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3) * Float.BYTES);
        
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, Cube.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3 + 2) * Float.BYTES);
        
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, Cube.VERTEX_SIZE_ELEMENTS * Float.BYTES, (3 + 3 + 2 + 2) * Float.BYTES);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindVertexArray(0);
        
        return vao;
    }
    
    public static void updateLightMapSize(int width, int height) {
        VerticesStream vertices = generateVertices(width, height);
        
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.vertices(), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    private static VerticesStream generateVertices(int width, int height) {
        VerticesStream stream = new VerticesStream(CubeTexture.TEXTURE_WIDTH, CubeTexture.TEXTURE_HEIGHT, width, height);

        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float scaleZ = 1.0f;

        float sizeX = 0.5f * scaleX;
        float sizeY = 0.5f * scaleY;
        float sizeZ = 0.5f * scaleZ;

        float xP = sizeX;
        float xN = -sizeX;
        float yP = sizeY;
        float yN = -sizeY;
        float zP = sizeZ;
        float zN = -sizeZ;

        //TOP
        stream.offset();
        stream.vertex(xN, yP, zP, 0f, 1f, 0f, 85f, 352f, 0f, 0f);
        stream.vertex(xP, yP, zP, 0f, 1f, 0f, 212f, 352f, 1f, 0f);
        stream.vertex(xN, yP, zN, 0f, 1f, 0f, 85f, 479f, 0f, 1f);
        stream.vertex(xP, yP, zN, 0f, 1f, 0f, 212f, 479f, 1f, 1f);
        stream.quad(0, 3, 2, 0, 1, 3);
        
        //BOTTOM
        stream.offset();
        stream.vertex(xN, yN, zP, 0f, -1f, 0f, 425f, 352f, 1f, 0f);
        stream.vertex(xN, yN, zN, 0f, -1f, 0f, 425f, 479f, 1f, 1f);
        stream.vertex(xP, yN, zP, 0f, -1f, 0f, 298f, 352f, 0f, 0f);
        stream.vertex(xP, yN, zN, 0f, -1f, 0f, 298f, 479f, 0f, 1f);
        stream.quad(0, 3, 2, 0, 1, 3);
        
        //LEFT
        stream.offset();
        stream.vertex(xN, yP, zN, -1f, 0f, 0f, 85f, 319f, 0f, 1f);
        stream.vertex(xN, yN, zN, -1f, 0f, 0f, 85f, 192f, 0f, 0f);
        stream.vertex(xN, yN, zP, -1f, 0f, 0f, 212f, 192f, 1f, 0f);
        stream.vertex(xN, yP, zP, -1f, 0f, 0f, 212f, 319f, 1f, 1f);
        stream.quad(0, 1, 3, 1, 2, 3);
        
        //RIGHT
        stream.offset();
        stream.vertex(xP, yP, zN, 1f, 0f, 0f, 425f, 319f, 1f, 1f);
        stream.vertex(xP, yN, zP, 1f, 0f, 0f, 298f, 192f, 0f, 0f);
        stream.vertex(xP, yN, zN, 1f, 0f, 0f, 425f, 192f, 1f, 0f);
        stream.vertex(xP, yP, zP, 1f, 0f, 0f, 298f, 319f, 0f, 1f);
        stream.quad(0, 3, 2, 2, 3, 1);
        
        //FRONT
        stream.offset();
        stream.vertex(xN, yP, zN, 0f, 0f, -1f, 212f, 159f, 1f, 1f);
        stream.vertex(xP, yN, zN, 0f, 0f, -1f, 85f, 32f, 0f, 0f);
        stream.vertex(xN, yN, zN, 0f, 0f, -1f, 212f, 32f, 1f, 0f);
        stream.vertex(xP, yP, zN, 0f, 0f, -1f, 85f, 159f, 0f, 1f);
        stream.quad(0, 3, 2, 3, 1, 2);
        
        //BACK
        stream.offset();
        stream.vertex(xN, yP, zP, 0f, 0f, 1f, 298f, 159f, 0f, 1f);
        stream.vertex(xN, yN, zP, 0f, 0f, 1f, 298f, 32f, 0f, 0f);
        stream.vertex(xP, yN, zP, 0f, 0f, 1f, 425f, 32f, 1f, 0f);
        stream.vertex(xP, yP, zP, 0f, 0f, 1f, 425f, 159f, 1f, 1f);
        stream.quad(0, 1, 3, 3, 1, 2);
        
        lightmapWidth = width;
        lightmapHeight = height;
        
        
        return stream;
    }
    
    private CubeVAO() {

    }

}
