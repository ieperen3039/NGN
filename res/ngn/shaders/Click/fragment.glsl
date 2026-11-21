#version 330

in vec3 mVertexPosition;
out vec4 fragColor;

uniform ivec3 color;

void main() {
    vec3 fColor = color;
    fragColor = vec4(fColor / 255.0, 1.0);
}
