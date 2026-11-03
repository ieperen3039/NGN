#version 330

layout(location = 0) out vec4 fragColor;

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

const int MAX_POINT_LIGHTS = 8;
const int MAX_DIRECTIONAL_LIGHTS = 4;

const float ATT_LIN = 0.1;
const float ATT_EXP = 0.01;

const float SCREEN_GAMMA = 2.2; // Assume the monitor is calibrated to the sRGB color space

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLights[MAX_DIRECTIONAL_LIGHTS];

uniform sampler2D texture_sampler;
uniform bool hasTexture;

uniform vec3 ambientLight;
uniform float specularPower;

uniform vec3 cameraPosition;
uniform mat4 viewProjectionMatrix;

// global variables
vec4 diffuse_color;
vec4 specular_color;

// Blinn-Phong lighting
// calculates the diffuse and specular color component caused by one light
vec3 calcBlinnPhong(vec3 light_color, vec3 position, vec3 light_direction, vec3 normal, float light_intensity) {
    // Diffuse component
    float lambertian = max(dot(normal, light_direction), 0.0);
    vec3 diffuse = diffuse_color * lambertian;

    vec3 view_direction = normalize(-position);
    vec3 half_direction = normalize(light_direction + view_direction);
    float specular_angle = max(dot(half_direction, normal), 0.0);
    float specular_factor = pow(specular_angle, specularPower);
    vec3 specular = specular_color * specular_factor

    return (diffuse + specular) * lightColor * lightPower / distance;
}

// caluclates the color addition caused by a point-light
vec3 calcPointLightComponents(PointLight light) {
    if (light.intensity == 0) return vec3(0, 0, 0);

    vec3 light_direction = light.mPosition - mVertexPosition;
    
    float distance = length(light_direction);
    float attenuation = (1.0 / (1.0 + ATT_LIN * distance + ATT_EXP * distance * distance));

    if (attenuation < (1 / 256)) {
        return vec3(0, 0, 0);
    } else {
        float attenuatedIntensity = attenuation * light.intensity;
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
    vec3 diffuse_specular = vec3(0.0, 0.0, 0.0);

    // Calculate directional light
    for (int i = 0; i < MAX_DIRECTIONAL_LIGHTS; i++) {
        diffuse_specular += calcDirectionalLightComponents(directionalLights[i]);
    }

    // Calculate Point Lights
    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
        diffuse_specular += calcPointLightComponents(pointLights[i]);
    }

    vec3 fragColor = diffuse_color * ambientLight + diffuse_specular;
    // apply gamma correction (assume ambientColor, diffuseColor and specColor
    // have been linearized, i.e. have no gamma correction in them)
    vec3 fragColorGammaCorrected = pow(diffuse_specular, vec3(1.0 / SCREEN_GAMMA));
    // use the gamma corrected color in the fragment
    gl_FragColor = vec4(colorGammaCorrected, 1.0);

}
