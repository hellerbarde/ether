package ch.fhnw.pstark.woipv;



/* ============================================================================
 * Copyright 2011,2012 Nicolas P. Rougier. All rights reserved.
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
 * ============================================================================
 *
 * Adaptations for Java done by Philip Stark
 *
 */






import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class DistanceFieldUtils {

	public static double[] make_distance_mapd( double[] data, int width, int height )
	{
	    short[] xdist = new short[width*height]; //(short *)  malloc( width * height * sizeof(short) );
	    short[] ydist = new short[width*height]; //(short *)  malloc( width * height * sizeof(short) );
	    double[] gx   = new double[width*height]; //(double *) calloc( width * height, sizeof(double) );
	    double[] gy      = new double[width*height]; //(double *) calloc( width * height, sizeof(double) );
	    double[] outside = new double[width*height]; //(double *) calloc( width * height, sizeof(double) );
	    double[] inside  = new double[width*height]; //(double *) calloc( width * height, sizeof(double) );
	    double vmin = Double.MAX_VALUE;
	    int i;

	    // Compute outside = edtaa3(bitmap); % Transform background (0's)
	    computegradient( data, width, height, gx, gy);
	    edtaa3(data, gx, gy, width, height, xdist, ydist, outside);
	    for( i=0; i<width*height; ++i)
	        if( outside[i] < 0.0 )
	            outside[i] = 0.0;

	    // Compute inside = edtaa3(1-bitmap); % Transform foreground (1's)
//	    memset( gx, 0, sizeof(double)*width*height );
//	    memset( gy, 0, sizeof(double)*width*height );
	    for( i=0; i<width*height; ++i)
	        data[i] = 1 - data[i];
	    computegradient( data, width, height, gx, gy );
	    edtaa3( data, gx, gy, width, height, xdist, ydist, inside );
	    for( i=0; i<width*height; ++i )
	        if( inside[i] < 0 )
	            inside[i] = 0.0;

	    // distmap = outside - inside; % Bipolar distance field
	    for( i=0; i<width*height; ++i)
	    {
	        outside[i] -= inside[i];
	        if( outside[i] < vmin )
	            vmin = outside[i];
	    }

	    vmin = Math.abs(vmin);

	    for( i=0; i<width*height; ++i)
	    {
	        float v = (float)outside[i];
	        if     ( v < -vmin) outside[i] = -vmin;
	        else if( v > +vmin) outside[i] = +vmin;
	        data[i] = (outside[i]+vmin)/(2*vmin);
	    }

	    // For good measure ;)
	    xdist = null;
	    ydist = null;
	    gx = null;
	    gy = null;
	    outside = null;
	    inside = null;
	    return data;
	}

	public static byte[] make_distance_mapb(byte[] img, int width, int height )
	{
	    double[] data    = new double[width*height];//(double *) calloc( width * height, sizeof(double) );
	    byte[] out = new byte[width*height];
	    //unsigned char *out = (unsigned char *) malloc( width * height * sizeof(unsigned char) );
	    int i;

	    // find minimum and maximum values
	    double img_min = Double.MAX_VALUE;
	    double img_max = Double.MIN_VALUE;

	    for( i=0; i<width*height; ++i)
	    {
	        double v = Byte.toUnsignedInt(img[i]);//((int)img[i])+128;
	        //double v = img[i];
	        data[i] = v;
	        if (v > img_max)
	            img_max = v;
	        if (v < img_min)
	            img_min = v;
	    }

	    // Map values from 0 - 255 to 0.0 - 1.0
	    for( i=0; i<width*height; ++i){
	    	int val = Byte.toUnsignedInt(img[i]);//((int)img[i])+128;
	    	data[i] = ((val-img_min)/img_max);
	    }

	    data = make_distance_mapd(data, width, height);

	    // map values from 0.0 - 1.0 to 0 - 255
	    for( i=0; i<width*height; ++i){
	    	Double value = 255*(1-data[i]);
//	    	if (value > 127){
//	    		value = value - 256;
//	    	}
	        out[i] = value.byteValue();
	    }
	    
	    data = null;

	    return out;
	}

	/*
	 * Compute the local gradient at edge pixels using convolution filters. The
	 * gradient is computed only at edge pixels. At other places in the image,
	 * it is never used, and it's mostly zero anyway.
	 */
	public static void computegradient(double[] img, int w, int h, double[] gx, double[] gy) {
		// DataBufferByte d = ((DataBufferByte) bi.getData().getDataBuffer()).;
		int i, j, k;
		double glength;
		float SQRT2 = 1.4142136f;
		for (i = 1; i < h - 1; i++) { // Avoid edges where the kernels would
										// spill over
			for (j = 1; j < w - 1; j++) {
				k = i * w + j;
				if ((img[k] > 0.0) && (img[k] < 1.0)) { // Compute gradient for
														// edge pixels only
					gx[k] = -img[k - w - 1] - SQRT2 * img[k - 1] - img[k + w - 1] + img[k - w + 1] + SQRT2 * img[k + 1]
							+ img[k + w + 1];
					gy[k] = -img[k - w - 1] - SQRT2 * img[k - w] - img[k + w - 1] + img[k - w + 1] + SQRT2 * img[k + w]
							+ img[k + w + 1];
					glength = gx[k] * gx[k] + gy[k] * gy[k];
					if (glength > 0.0) { // Avoid division by zero
						glength = Math.sqrt(glength);
						gx[k] = gx[k] / glength;
						gy[k] = gy[k] / glength;
					}
				}
			}
		}

		// TODO: Compute reasonable values for gx, gy also around the image
		// edges.
		// (These are zero now, which reduces the accuracy for a 1-pixel wide
		// region
		// around the image edge.) 2x2 kernels would be suitable for this.
	}

	/*
	 * A somewhat tricky function to approximate the distance to an edge in a
	 * certain pixel, with consideration to either the local gradient (gx,gy) or
	 * the direction to the pixel (dx,dy) and the pixel greyscale value a. The
	 * latter alternative, using (dx,dy), is the metric used by edtaa2(). Using
	 * a local estimate of the edge gradient (gx,gy) yields much better accuracy
	 * at and near edges, and reduces the error even at distant pixels provided
	 * that the gradient direction is accurately estimated.
	 */
	public static double edgedf(double gx, double gy, double a)
	{
	    double df, glength, temp, a1;

	    if ((gx == 0) || (gy == 0)) { // Either A) gu or gv are zero, or B) both
	        df = 0.5-a;  // Linear approximation is A) correct or B) a fair guess
	    } else {
	        glength = Math.sqrt(gx*gx + gy*gy);
	        if(glength>0) {
	            gx = gx/glength;
	            gy = gy/glength;
	        }
	        /* Everything is symmetric wrt sign and transposition,
	         * so move to first octant (gx>=0, gy>=0, gx>=gy) to
	         * avoid handling all possible edge directions.
	         */
	        gx = Math.abs(gx);
	        gy = Math.abs(gy);
	        if(gx<gy) {
	            temp = gx;
	            gx = gy;
	            gy = temp;
	        }
	        a1 = 0.5*gy/gx;
	        if (a < a1) { // 0 <= a < a1
	            df = 0.5*(gx + gy) - Math.sqrt(2.0*gx*gy*a);
	        } else if (a < (1.0-a1)) { // a1 <= a <= 1-a1
	            df = (0.5-a)*gx;
	        } else { // 1-a1 < a <= 1
	            df = -0.5*(gx + gy) + Math.sqrt(2.0*gx*gy*(1.0-a));
	        }
	    }
	    return df;
	}

	public static double distaa3(double[] img, double[] gximg, double[] gyimg, int w, int c, int xc, int yc, int xi, int yi)
	{
	  double di, df, dx, dy, gx, gy, a;
	  int closest;

	  closest = c-xc-yc*w; // Index to the edge pixel pointed to from c
	  a = img[closest];    // Grayscale value at the edge pixel
	  gx = gximg[closest]; // X gradient component at the edge pixel
	  gy = gyimg[closest]; // Y gradient component at the edge pixel

	  if(a > 1.0) a = 1.0;
	  if(a < 0.0) a = 0.0; // Clip grayscale values outside the range [0,1]
	  if(a == 0.0) return 1000000.0; // Not an object pixel, return "very far" ("don't know yet")

	  dx = (double)xi;
	  dy = (double)yi;
	  di = Math.sqrt(dx*dx + dy*dy); // Length of integer vector, like a traditional EDT
	  if(di==0) { // Use local gradient only at edges
	      // Estimate based on local gradient only
	      df = edgedf(gx, gy, a);
	  } else {
	      // Estimate gradient based on direction to edge (accurate for large di)
	      df = edgedf(dx, dy, a);
	  }
	  return di + df; // Same metric as edtaa2, except at edges (where di=0)
	}
	
	
	// Shorthand macro: add ubiquitous parameters dist, gx, gy, img and w and call distaa3()
	//#define DISTAA(c,xc,yc,xi,yi) (distaa3(img, gx, gy, w, c, xc, yc, xi, yi))
	
	
	public static void edtaa3(double[] img, double[] gx, double[] gy, int w, int h, short[] distx, short[] disty, double[] dist)
	{
	  int x, y, i, c;
	  int offset_u, offset_ur, offset_r, offset_rd,
	  offset_d, offset_dl, offset_l, offset_lu;
	  double olddist, newdist;
	  int cdistx, cdisty, newdistx, newdisty;
	  int changed;
	  double epsilon = 1e-3;

	  /* Initialize index offsets for the current image width */
	  offset_u = -w;
	  offset_ur = -w+1;
	  offset_r = 1;
	  offset_rd = w+1;
	  offset_d = w;
	  offset_dl = w-1;
	  offset_l = -1;
	  offset_lu = -w-1;

	  /* Initialize the distance images */
	  for(i=0; i<w*h; i++) {
	    distx[i] = 0; // At first, all pixels point to
	    disty[i] = 0; // themselves as the closest known.
	    if(img[i] <= 0.0)
	      {
		dist[i]= 1000000.0; // Big value, means "not set yet"
	      }
	    else if (img[i]<1.0) {
	      dist[i] = edgedf(gx[i], gy[i], img[i]); // Gradient-assisted estimate
	    }
	    else {
	      dist[i]= 0.0; // Inside the object
	    }
	  }

	  /* Perform the transformation */
	  do
	    {
	      changed = 0;

	      /* Scan rows, except first row */
	      for(y=1; y<h; y++)
	        {

	          /* move index to leftmost pixel of current row */
	          i = y*w;

	          /* scan right, propagate distances from above & left */

	          /* Leftmost pixel is special, has no left neighbors */
	          olddist = dist[i];
	          if(olddist > 0) // If non-zero distance or not set yet
	            {
		      c = i + offset_u; // Index of candidate for testing
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx;
	              newdisty = cdisty+1;
	              //DISTAA(c,xc,yc,xi,yi) (distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty))
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_ur;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty+1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  changed = 1;
	                }
	            }
	          i++;

	          /* Middle pixels have all neighbors */
	          for(x=1; x<w-1; x++, i++)
	            {
	              olddist = dist[i];
	              if(olddist <= 0) continue; // No need to update further

		      c = i+offset_l;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_lu;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty+1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_u;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx;
	              newdisty = cdisty+1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_ur;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty+1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  changed = 1;
	                }
	            }

	          /* Rightmost pixel of row is special, has no right neighbors */
	          olddist = dist[i];
	          if(olddist > 0) // If not already zero distance
	            {
		      c = i+offset_l;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_lu;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty+1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_u;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx;
	              newdisty = cdisty+1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  changed = 1;
	                }
	            }

	          /* Move index to second rightmost pixel of current row. */
	          /* Rightmost pixel is skipped, it has no right neighbor. */
	          i = y*w + w-2;

	          /* scan left, propagate distance from right */
	          for(x=w-2; x>=0; x--, i--)
	            {
	              olddist = dist[i];
	              if(olddist <= 0) continue; // Already zero distance

		      c = i+offset_r;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  changed = 1;
	                }
	            }
	        }

	      /* Scan rows in reverse order, except last row */
	      for(y=h-2; y>=0; y--)
	        {
	          /* move index to rightmost pixel of current row */
	          i = y*w + w-1;

	          /* Scan left, propagate distances from below & right */

	          /* Rightmost pixel is special, has no right neighbors */
	          olddist = dist[i];
	          if(olddist > 0) // If not already zero distance
	            {
		      c = i+offset_d;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_dl;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  changed = 1;
	                }
	            }
	          i--;

	          /* Middle pixels have all neighbors */
	          for(x=w-2; x>0; x--, i--)
	            {
	              olddist = dist[i];
	              if(olddist <= 0) continue; // Already zero distance

		      c = i+offset_r;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_rd;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_d;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
	                  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_dl;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
	                  dist[i]=newdist;
	                  changed = 1;
	                }
	            }
	          /* Leftmost pixel is special, has no left neighbors */
	          olddist = dist[i];
	          if(olddist > 0) // If not already zero distance
	            {
		      c = i+offset_r;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
	                  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_rd;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx-1;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
			  dist[i]=newdist;
	                  olddist=newdist;
	                  changed = 1;
	                }

		      c = i+offset_d;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx;
	              newdisty = cdisty-1;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
	                  dist[i]=newdist;
	                  changed = 1;
	                }
	            }

	          /* Move index to second leftmost pixel of current row. */
	          /* Leftmost pixel is skipped, it has no left neighbor. */
	          i = y*w + 1;
	          for(x=1; x<w; x++, i++)
	            {
	              /* scan right, propagate distance from left */
	              olddist = dist[i];
	              if(olddist <= 0) continue; // Already zero distance

		      c = i+offset_l;
		      cdistx = distx[c];
		      cdisty = disty[c];
	              newdistx = cdistx+1;
	              newdisty = cdisty;
	              newdist = distaa3(img, gx, gy, w, c, cdistx, cdisty, newdistx, newdisty);
	              if(newdist < olddist-epsilon)
	                {
	                  distx[i]=(short)newdistx;
	                  disty[i]=(short)newdisty;
	                  dist[i]=newdist;
	                  changed = 1;
	                }
	            }
	        }
	    }
	  while(changed > 0); // Sweep until no more updates are made

	  /* The transformation is completed. */

	}

}
