/*
 * Copyright (C) 2011 by Stefan Rothe
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
package ch.jeda.blocks;

class AddEntityEvent extends MapEvent {

    private final Entity entity;
    private final Field field;
    private final BlockMap map;

    AddEntityEvent(BlockMap map, Field field, Entity entity) {
        this.entity = entity;
        this.field = field;
        this.map = map;
    }

    @Override
    void evaluate() {
        this.map.doAddEntity(this.entity);
        this.entity.doSetMap(this.map);
        this.entity.doSetField(this.field, true);
    }
}