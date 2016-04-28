package com.uwsoft.editor.utils;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.uwsoft.editor.renderer.components.TransformComponent;

/**
 * Created by Sasun Poghosyan on 4/18/2016.
 */
public class TransformUtils {

    private static final Matrix3 tempMat = new Matrix3();

    public static float hypotenuse(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static Vector2 transform(float translationX, float translationY, float scaleX, float scaleY, float angle, float pointX, float pointY) {
        Matrix3 matrix = new Matrix3();
        Vector2 vector = new Vector2(pointX, pointY);
        matrix.translate(translationX, translationY).rotate(angle).scale(scaleX, scaleY).translate(-translationX, -translationY);
        vector.mul(matrix);
        return vector;
    }

    public static Matrix3 transform(float translationX, float translationY, float scaleX, float scaleY, float angle) {
        Matrix3 matrix = new Matrix3();
        matrix.translate(translationX, translationY).rotate(angle).scale(scaleX, scaleY).translate(-translationX, -translationY);
        return matrix;
    }

    public static Matrix3 transform(TransformComponent transformComponent) {
        float translationX = transformComponent.x + transformComponent.originX;
        float translationY = transformComponent.y + transformComponent.originY;
        float scaleX = transformComponent.scaleX;
        float scaleY = transformComponent.scaleY;
        float angle = transformComponent.rotation;
        tempMat.idt();
        tempMat.translate(translationX, translationY).rotate(angle).scale(scaleX, scaleY).translate(-translationX, -translationY);
        return tempMat;
    }

}
