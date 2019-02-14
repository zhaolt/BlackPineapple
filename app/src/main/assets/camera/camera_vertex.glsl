attribute vec4 aPosition;
attribute vec4 aTexCoord;
varying vec2 vTexCoord;
uniform mat4 aMvpMatrix;
uniform mat4 aStMatrix;

void main() {
    gl_Position = aMvpMatrix * aPosition;
    vTexCoord = (aStMatrix * aTexCoord).xy;
}
