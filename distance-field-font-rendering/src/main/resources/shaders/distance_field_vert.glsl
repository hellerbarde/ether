#version 330

#include <view_block.glsl>

//uniform vec4 materialColor;

in vec4 vertexPosition;
in vec4 vertexColor;
in vec2 vertexTexCoord;

//out vec4 vsColor;
out vec2 vsTexCoord;
/*out vec3 vs_fill_color;
out vec3 vs_line_color;
out vec3 vs_glow_color;*/

void main() {
	//vsColor = vec4(0.5,0.5,0.5,0.5);
	/*vs_fill_color = fill_color;
	vs_line_color = line_color;
	vs_glow_color = glow_color;*/
	vsTexCoord = vertexTexCoord;
	gl_Position = view.viewProjMatrix * vertexPosition;
}


/*#version 330


uniform mat4 u_model;
uniform mat4 u_view;
uniform mat4 u_projection;
uniform vec4 u_color;

in vec3 vertex;
in vec2 tex_coord;
in vec4 color;

void main(void)
{
    gl_TexCoord[0].xy = tex_coord.xy;
    gl_FrontColor     = color * u_color;
    gl_Position       = u_projection*(u_view*(u_model*vec4(vertex,1.0)));
}*/
