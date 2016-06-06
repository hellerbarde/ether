#version 330
/* =========================================================================
 * Copyright 2011 Nicolas P. Rougier. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY NICOLAS P. ROUGIER ''AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL NICOLAS P. ROUGIER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Nicolas P. Rougier.
 * =========================================================================
  Modifications for use with ether by Philip Stark
  The modifications are licensed under the MIT License.
 */
uniform sampler2D colorMap;

in vec4 vsColor;
in vec2 vsTexCoord;

out vec4 FragColor;

vec3 fill_color    = vec3(1.0,1.0,1.0);
const float glyph_center   = 0.50;

/*vec3 line_color  = vec3(0.0,0.0,0.0);
const float outline_center = 0.55;
vec3 glow_color     = vec3(1.0,1.0,1.0);
const float glow_center    = 1.25;*/

void main(void)
{

    vec4  color = texture(colorMap, vsTexCoord);
    float dist  = color.r;

    // Calculate the width according to the x and y derivatives of the
    // distance value
    float width = fwidth(dist);

    float alpha = smoothstep(glyph_center-width, glyph_center+width, dist);

    // pass through raw values (for debugging) -------------------------------
    //FragColor = vec4(color.r,color.r,color.r,1);

    // Just Fill ----------------------------------------------------------
    FragColor = vec4(fill_color, alpha);

    // Fill + Outline --------------------------------------------------------
    /*float mu = smoothstep(outline_center-width, outline_center+width, dist);
    vec3 rgb = mix(line_color, fill_color, mu);
    FragColor = vec4(rgb, max(alpha,mu));*/







    // Glow + outline --------------------------------------------------------
    // outside the fill, use the glow color.
    /* since alpha has a small width window, this is basically:
    if (alpha)
      return fill color
    else
      return glow color */
    /*vec3 rgb = mix(glow_color, fill_color, alpha);*/

    // Calculate where in the glow we are.
    /*float mu = smoothstep(glyph_center, glow_center, dist);*/
    //float mu = smoothstep(glyph_center, glow_center, sqrt(dist));

    // if we are in the fill, paint opaque.
    /*color = vec4(rgb, max(alpha, mu));*/

    // Are we in the outline?
    /*float beta = smoothstep(outline_center-width, outline_center+width, dist);*/

    // if we are in the outline, paint the outline color, else the
    // fill or glow
    /*rgb = mix(line_color, color.rgb, beta);*/

    // construct the return color.
    //FragColor = vec4(rgb, max(color.a, beta));

}
