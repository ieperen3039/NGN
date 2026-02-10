#version 330

layout(location = 0) out vec4 fragColor;
layout(location = 1) out float outColorR;
layout(location = 2) out float outColorG;
layout(location = 3) out float outColorB;

// normal of the vertex
in vec3 mVertexNormal;
// position of the vertex
in vec3 mVertexPosition;
// texture coordinates
in vec2 mTexCoord;
// color transformation
in vec4 mColor;

struct PointLight
{
    vec3 color;
    vec3 mPosition;
    float intensity;
};

struct DirectionalLight
{
    vec3 color;
    vec3 mPosition; // actually direction
    float intensity;
};

struct Material
{
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

const int MAX_POINT_LIGHTS = 2;
const int MAX_DIRECTIONAL_LIGHTS = 8;

const float ATT_LIN = 0.1;
const float ATT_EXP = 0.01;

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLights[MAX_DIRECTIONAL_LIGHTS];

uniform sampler2D texture_sampler;
uniform bool hasTexture;

uniform vec3 ambientLight;
uniform float specularPower;

uniform vec3 cameraPosition;
uniform mat4 viewProjectionMatrix;

uniform bool renderToBuffer;

// global variables
vec4 diffuse_color;
vec4 specular_color;

// Blinn-Phong lighting
// calculates the diffuse and specular color component caused by one light
vec3 calcBlinnPhong(vec3 light_color, vec3 position, vec3 light_direction, vec3 normal, float light_intensity) {
    // Diffuse component
    float diff = max(dot(normal, light_direction), 0.0);
    vec3 diffuse = diffuse_color.xyz * light_color * diff;

    // (phong) specular
    vec3 lightReflect = reflect(light_direction, normal);
    vec3 virtualLightPosition = normalize(-lightReflect);
    float linearSpec = max(0.0, dot(virtualLightPosition, normalize(cameraPosition)));
    float shine = pow(linearSpec, specularPower * material.reflectance);
    vec3 specular = shine * light_color * material.specular.xyz * material.specular.w;

    return (diffuse + specular) * light_intensity;
}

// Calculate Attenuation
// calculates the falloff of light on a given distance vector
float calcAttenuation(vec3 light_direction) {
    float distance = length(light_direction);
    return (1.0 / (1.0 + ATT_LIN * distance + ATT_EXP * distance * distance));
}

// caluclates the color addition caused by a point-light
vec3 calcPointLightComponents(PointLight light) {
    if (light.intensity == 0) return vec3(0, 0, 0);

    vec3 light_direction = light.mPosition - mVertexPosition;
    float att = calcAttenuation(light_direction);

    if (att == 0) {
        return vec3(0, 0, 0);
    } else {
        float attenuatedIntensity = att * light.intensity;
        return calcBlinnPhong(light.color, mVertexPosition, normalize(light_direction), mVertexNormal, attenuatedIntensity);
    }
}

// caluclates the color addition caused by an infinitely far away light, including shadows.
vec3 calcDirectionalLightComponents(DirectionalLight light) {
    if (light.intensity == 0.0){
        return vec3(0, 0, 0);

    } else {
        vec3 nLightDir = normalize(-light.mPosition);
        return calcBlinnPhong(light.color, mVertexPosition, nLightDir, mVertexNormal, light.intensity);
    }
}

void main() {
    if (hasTexture) {
        diffuse_color = texture(texture_sampler, mTexCoord);

    } else {
        diffuse_color = mColor * material.diffuse;
    }

    specular_color = material.specular;

    // diffuse and specular color accumulator
    vec3 diffuseSpecular = vec3(0.0, 0.0, 0.0);

    // Calculate directional light
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[0]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[1]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[2]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[3]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[4]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[5]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[6]);
    diffuseSpecular += calcDirectionalLightComponents(directionalLights[7]);

    // Calculate Point Lights
    diffuseSpecular += calcPointLightComponents(pointLights[0]);
    diffuseSpecular += calcPointLightComponents(pointLights[1]);

    vec4 col = diffuse_color * vec4(ambientLight, 1.0) + vec4(diffuseSpecular, 0.0);

    if (renderToBuffer) {
        outColorR = col.r;
        outColorG = col.g;
        outColorB = col.b;
    } else {
        fragColor = col;
    }
}
