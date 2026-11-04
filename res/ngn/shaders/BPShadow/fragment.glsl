#version 330

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
    vec3 mDirection;
    float intensity;
    mat4 lightSpaceMatrix;
};

struct Material
{
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};

const int MAX_POINT_LIGHTS = 16;
const float SHADOW_BIAS = 1.0/(1 << 12);

const float ATT_LIN = 0.1;
const float ATT_EXP = 0.01;

const float LINE_SIZE = 0.0002;
const float LINE_DENSITY = 100;// 500 seems to be the max
const float LINE_ALPHA = 0.3;
const float LINE_COLOR_SENSITIVITY = 2;
const float MINIMUM_LINE_DIST = 0.1;

uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLight;

uniform sampler2D texture_sampler;
uniform bool hasTexture;
uniform bool doDrawHeightLines;

uniform vec3 ambientLight;
uniform float specularPower;
uniform vec3 cameraPosition;

uniform sampler2D dynamicShadowMap;
uniform mat4 viewProjectionMatrix;

// global variables
vec4 diffuse_color;
vec4 specular_color;

// Blinn-Phong lighting
// calculates the diffuse and specular color component caused by one light
vec3 calcBlinnPhong(vec3 light_color, vec3 eye_position, vec3 light_direction, vec3 normal, float light_intensity) {
    // Diffuse component
    float lambertian = max(dot(normal, light_direction), 0.0);
    vec3 diffuse = diffuse_color.xyz * lambertian;

    vec3 view_direction = normalize(eye_position);
    vec3 half_direction = normalize(light_direction + view_direction);
    float specular_angle = max(dot(half_direction, normal), 0.0);
    float specular_factor = pow(specular_angle, specularPower * material.reflectance);
    vec3 specular = specular_color.xyz * specular_factor;

    return (diffuse + specular) * light_color * light_intensity;
}

// Calculate Attenuation
// calculates the falloff of light on a given distance vector
float calcAttenuation(vec3 light_position) {
    float distance = length(light_position);
    return (1.0 / (1.0 + ATT_LIN * distance + ATT_EXP * distance * distance));
}

// calculates how much light should remain due to shadow
float calcShadow2D(mat4 lsMatrix, vec3 vPosition, vec3 vNormal, vec3 nLightDir, sampler2D shadowMap) {
    vec4 coord = lsMatrix * vec4(vPosition, 1.0);
    if (coord.z < -1 || coord.z > 1) return 1.0;

    vec3 projCoords = coord.xyz / coord.w;
    projCoords = projCoords * 0.5 + 0.5;

    if (projCoords.x < 0 || projCoords.x > 1) return 1.0;
    if (projCoords.y < 0 || projCoords.y > 1) return 1.0;

    float bias = max(0.01 * (1.0 - dot(vNormal, nLightDir)), SHADOW_BIAS);
    float currentDepth = projCoords.z;
    ivec2 resolution = textureSize(shadowMap, 0);
    ivec2 pixCoord = ivec2(round(projCoords.xy * resolution));

    float light = 0.0;
    {
        float pcfDepth = texelFetch(shadowMap, pixCoord, 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(0, -1), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(-1, 0), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(0, 1), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }{
        float pcfDepth = texelFetch(shadowMap, pixCoord + ivec2(1, 0), 0).r;
        float addition = currentDepth - bias < pcfDepth ? 1.0 : 0.0;
        light += addition;
    }

    light = min(1.0, light / 5.0);

    return light;
}

// caluclates the color addition caused by a point-light
vec3 calcPointLightComponents(PointLight light) {
    if (light.intensity == 0) return vec3(0, 0, 0);

    vec3 vLightPosition = light.mPosition - mVertexPosition;
    vec3 vEyePosition = cameraPosition - mVertexPosition;

    float distance = length(vLightPosition);
    float attenuation = (1.0 / (1.0 + ATT_LIN * distance + ATT_EXP * distance * distance));

    if (attenuation < (1.0 / 256.0)) {
        return vec3(0, 0, 0);
    } else {
        float attenuatedIntensity = attenuation * light.intensity;
        return calcBlinnPhong(light.color, vEyePosition, normalize(vLightPosition), mVertexNormal, attenuatedIntensity);
    }
}

// caluclates the color addition caused by an infinitely far away light, including shadows.
vec3 calcDirectionalLightComponents(DirectionalLight light) {
    if (light.intensity == 0.0){
        return vec3(0, 0, 0);

    } else {
        vec3 nvLightDirection = normalize(light.mDirection);
        vec3 vEyePosition = cameraPosition - mVertexPosition;
        float dynamicShadow = calcShadow2D(light.lightSpaceMatrix, mVertexPosition, mVertexNormal, nvLightDirection, dynamicShadowMap);
        if (dynamicShadow == 0) return vec3(0, 0, 0);

        vec3 component = calcBlinnPhong(light.color, vEyePosition, nvLightDirection, mVertexNormal, light.intensity);
        return component * dynamicShadow;
    }
}

float sigm(float x){
    return x / sqrt(1 + x * x * x);
}

vec4 drawHeightLines(vec4 original_color) {
    float line_visibility = 0;

    float camera_dist = length(cameraPosition - mVertexPosition);
    float target_line_dist = (camera_dist) / LINE_DENSITY;
    float target_level_dist = target_line_dist / MINIMUM_LINE_DIST;

    // calculate distance between markers
    int marker_shift = max(0, int(floor(log2(target_level_dist))));
    int marker_level_dist = 1 << min(marker_shift, 31);

    // calculate distance to nearest marker
    float fragment_level = (mVertexPosition.z / MINIMUM_LINE_DIST);
    float distance_from_level = mod(fragment_level, marker_level_dist);
    float distance_from_height_line = distance_from_level * MINIMUM_LINE_DIST;

    if (abs(distance_from_height_line) < LINE_SIZE * camera_dist){
        // if the next marker_level_dist doesn't activate this, fade out
        if (distance_from_level != mod(fragment_level, marker_level_dist << 1)){
            // foo slightly above 0 means that the camera is almost close enough for solid
            float foo = ((target_level_dist - marker_level_dist) / marker_level_dist);
            line_visibility = max(0, 1 - foo);

        } else {
            line_visibility = 1;
        }
        line_visibility *= LINE_ALPHA;
    }

    float x_component = min(1, max(0, (LINE_COLOR_SENSITIVITY * mVertexNormal.x) + 0.5));
    float y_component = min(1, max(0, (LINE_COLOR_SENSITIVITY * mVertexNormal.y) + 0.5));
    vec4 line_component = vec4(max(y_component, 0), abs(x_component), max(-y_component, 0), 1.0);
    return (1 - line_visibility) * original_color + line_visibility * line_component;
}

void main() {
    if (hasTexture){
        diffuse_color = texture(texture_sampler, mTexCoord);

    } else {
        diffuse_color = mColor * material.diffuse;
    }

    specular_color = material.specular;

    // diffuse and specular color accumulator
    vec3 diffuse_specular = vec3(0.0, 0.0, 0.0);

    // Calculate directional light
    diffuse_specular += calcDirectionalLightComponents(directionalLight);

    // Calculate Point Lights
    diffuse_specular += calcPointLightComponents(pointLights[0]);
    diffuse_specular += calcPointLightComponents(pointLights[1]);
    diffuse_specular += calcPointLightComponents(pointLights[2]);
    diffuse_specular += calcPointLightComponents(pointLights[3]);
    diffuse_specular += calcPointLightComponents(pointLights[4]);
    diffuse_specular += calcPointLightComponents(pointLights[5]);
    diffuse_specular += calcPointLightComponents(pointLights[6]);
    diffuse_specular += calcPointLightComponents(pointLights[7]);
    diffuse_specular += calcPointLightComponents(pointLights[8]);
    diffuse_specular += calcPointLightComponents(pointLights[9]);
    diffuse_specular += calcPointLightComponents(pointLights[10]);
    diffuse_specular += calcPointLightComponents(pointLights[11]);
    diffuse_specular += calcPointLightComponents(pointLights[12]);
    diffuse_specular += calcPointLightComponents(pointLights[13]);
    diffuse_specular += calcPointLightComponents(pointLights[14]);
    diffuse_specular += calcPointLightComponents(pointLights[15]);

    if (doDrawHeightLines) {
        diffuse_specular = drawHeightLines(diffuse_specular);
    }

    vec3 fragColorRaw = diffuse_color.xyz * ambientLight + diffuse_specular;
    // apply gamma correction (assume ambientColor, diffuseColor and specColor
    // have been linearized, i.e. have no gamma correction in them)
    vec3 fragColorGammaCorrected = pow(fragColorRaw, vec3(1.0 / SCREEN_GAMMA));
    // use the gamma corrected color in the fragment
    fragColor = vec4(fragColorGammaCorrected, diffuse_color.w);
}