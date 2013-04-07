/*
 * Copyright (C) 2012 - 2013 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.jeda.platform.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import ch.jeda.Location;
import ch.jeda.Size;
import ch.jeda.Transformation;
import ch.jeda.platform.CanvasImp;
import ch.jeda.platform.ImageImp;
import ch.jeda.ui.Color;

class AndroidCanvasImp implements CanvasImp {

    private final Paint fillPaint;
    private final Paint pixelPaint;
    private final Paint strokePaint;
    private final Paint textPaint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Size size;

    @Override
    public void clear() {
        this.canvas.drawColor(0);
    }

    @Override
    public void copyFrom(final int x, final int y, final CanvasImp source) {
        assert source != null;
        assert source instanceof AndroidCanvasImp;

        this.canvas.drawBitmap(((AndroidCanvasImp) source).bitmap,
                               x, y, this.fillPaint);
    }

    @Override
    public void drawCircle(final int x, final int y, final int radius) {
        assert radius > 0;

        this.canvas.drawCircle(x, y, radius, this.strokePaint);
        this.modified();
    }

    @Override
    public void drawImage(final int x, final int y, final ImageImp image) {
        assert image != null;
        assert image instanceof AndroidImageImp;

        this.canvas.drawBitmap(((AndroidImageImp) image).bitmap, x, y,
                               this.fillPaint);
        this.modified();
    }

    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2) {
        this.canvas.drawLine(x1, y1, x2, y2, this.strokePaint);
        this.modified();
    }

    @Override
    public void drawPolygon(Iterable<Location> edges) {
        assert edges != null;

        this.canvas.drawPath(createPath(edges), this.strokePaint);
        this.modified();
    }

    @Override
    public void drawRectangle(final int x, final int y,
                              final int width, final int height) {
        this.canvas.drawRect(x, y, x + width, y + height, this.strokePaint);
        this.modified();
    }

    @Override
    public void drawText(final int x, final int y, final String text) {
        assert text != null;

        this.canvas.drawText(text, x, y - (int) this.textPaint.ascent(),
                             this.textPaint);
        this.modified();
    }

    @Override
    public void fill() {
        this.canvas.drawColor(this.fillPaint.getColor());
        this.modified();
    }

    @Override
    public void fillCircle(final int x, final int y, final int radius) {
        assert radius > 0;

        this.canvas.drawCircle(x, y, radius, this.fillPaint);
        this.modified();
    }

    @Override
    public void fillPolygon(Iterable<Location> edges) {
        assert edges != null;

        this.canvas.drawPath(createPath(edges), this.fillPaint);
        this.modified();
    }

    @Override
    public void fillRectangle(final int x, final int y,
                              final int width, final int height) {
        this.canvas.drawRect(x, y, x + width, y + height, this.fillPaint);
        this.modified();
    }

    @Override
    public double getLineWidth() {
        return this.strokePaint.getStrokeWidth();
    }

    @Override
    public Color getPixelAt(final int x, final int y) {
        assert this.size.contains(x, y);

        return new Color(this.bitmap.getPixel(x, y));
    }

    @Override
    public Size getSize() {
        return this.size;
    }

    @Override
    public void setAlpha(final int alpha) {
        assert 0 <= alpha && alpha <= 255;

        this.fillPaint.setAlpha(alpha);
    }

    public void setColor(final Color color) {
        this.fillPaint.setColor(color.value);
        this.strokePaint.setColor(color.value);
        this.textPaint.setColor(color.value);
    }

    @Override
    public void setFontSize(final int size) {
        this.strokePaint.setTextSize(size);
    }

    @Override
    public void setLineWidth(final double width) {
        this.strokePaint.setStrokeWidth((float) width);
    }

    @Override
    public void setPixelAt(final int x, final int y, final Color color) {
        assert this.size.contains(x, y);
        assert color != null;

        this.pixelPaint.setColor(color.value);
        this.canvas.drawPoint(x, y, this.pixelPaint);
    }

    @Override
    public void setTransformation(final Transformation transformation) {
        this.canvas.setMatrix(((AndroidTransformation) transformation).matrix);
    }

    @Override
    public ImageImp takeSnapshot() {
        return new AndroidImageImp(Bitmap.createBitmap(this.bitmap));
    }

    @Override
    public Size textSize(final String text) {
        Rect bounds = new Rect();
        this.strokePaint.getTextBounds(text, 0, text.length(), bounds);
        return new Size(bounds.width(), bounds.height());
    }

    AndroidCanvasImp() {
        this.fillPaint = new Paint();
        this.fillPaint.setStyle(Paint.Style.FILL);
        this.fillPaint.setAntiAlias(true);
        this.pixelPaint = new Paint();
        this.strokePaint = new Paint();
        this.strokePaint.setStyle(Paint.Style.STROKE);
        this.strokePaint.setAntiAlias(true);
        this.textPaint = new Paint();
    }

    Canvas getCanvas() {
        return this.canvas;
    }

    void modified() {
    }

    Bitmap getBitmap() {
        return this.bitmap;
    }

    final void setSize(final Size size) {
        this.size = size;
        this.bitmap = Bitmap.createBitmap(size.width, size.height, Config.ARGB_8888);
        this.canvas = new Canvas(this.bitmap);
    }

    private static Path createPath(Iterable<Location> edges) {
        Path result = new Path();
        boolean first = true;
        for (Location edge : edges) {
            if (first) {
                result.moveTo(edge.x, edge.y);
                first = false;
            }
            else {
                result.lineTo(edge.x, edge.y);
            }
        }

        result.close();
        return result;
    }
}
