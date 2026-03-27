#version 330 core
layout (location = 0) in vec2 position;
layout (location = 1) in vec2 texCoord;

out vec2 vTexCoord;

uniform float xOffset;
uniform float xScale;
uniform float yOffset;
uniform float yScale;

void main() {
    vTexCoord = texCoord;
    vTexCoord -= vec2(xOffset, yOffset);
    vTexCoord *= vec2(xScale, yScale);
    vTexCoord += vec2(0.5, 0.5);
    gl_Position = vec4(position, 0.0, 1.0);
}
