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

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 *
 * @author Cien
 */
public class Cube {
    
    public static final int VERTEX_SIZE_ELEMENTS = 3 + 3 + 2 + 2;
    public static final int NUMBER_OF_INDICES = 3 * 2 * 6;
    public static final int CUBE_TEXTURE = CubeTexture.CUBE_TEXTURE;
    public static final int SHADER_PROGRAM = CubeProgram.SHADER_PROGRAM;
    public static final int VAO = CubeVAO.VAO;
    
    public static void init() {
        
    }
    
    private final Matrix4f model = new Matrix4f();
    private final Matrix3f normalModel = new Matrix3f();
    
    public Cube(Matrix4fc model) {
        this.model.set(model);
        this.normalModel.set(new Matrix4f(model).invert().transpose());
    }

    public Matrix4f getModel() {
        return model;
    }

    public Matrix3f getNormalModel() {
        return normalModel;
    }
    
}
