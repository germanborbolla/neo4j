/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.nioneo.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.neo4j.kernel.impl.core.PropertyIndex;

/**
 * PropertyRecord is a container for PropertyBlocks. PropertyRecords form
 * a double linked list and each one holds one or more PropertyBlocks that
 * are the actual property key/value pairs. Because PropertyBlocks are of
 * variable length, a full PropertyRecord can be holding just one
 * PropertyBlock.
 *
 */
public class PropertyRecord extends Abstract64BitRecord
{
    // private long[] propBlock = new long[getPayloadSizeLongs()];
    private long nextProp = Record.NO_NEXT_PROPERTY.intValue();
    private long prevProp = Record.NO_NEXT_PROPERTY.intValue();
    private final List<PropertyBlock> blockRecords = new ArrayList<PropertyBlock>(
            3 );
    private long entityId = -1;
    private boolean nodeIdSet;
    private boolean isChanged;

    public PropertyRecord( long id )
    {
        super( id );
    }

    public void setNodeId( long nodeId )
    {
        nodeIdSet = true;
        entityId = nodeId;
    }

    public void setRelId( long relId )
    {
        nodeIdSet = false;
        entityId = relId;
    }

    public long getNodeId()
    {
        if ( nodeIdSet )
        {
            return entityId;
        }
        return -1;
    }

    public long getRelId()
    {
        if ( !nodeIdSet )
        {
            return entityId;
        }
        return -1;
    }

    /**
     * Gets the sum of the sizes of the blocks in this record, in bytes.
     *
     * @return
     */
    public int size()
    {
        int result = 0;
        for ( PropertyBlock block : blockRecords )
        {
            result += block.getSize();
        }
        return result;
    }

    /*
    public long[] getPropBlock()
    {
        return propBlock;
    }

    public long getSinglePropBlock()
    {
        return propBlock[0];
    }

    public void setPropBlock( long[] propBlock )
    {
        this.propBlock = propBlock.length < PropertyType.getPayloadSizeLongs() ?
                Arrays.copyOf( propBlock, PropertyType.getPayloadSizeLongs() ) : propBlock;
    }

    public void setSinglePropBlock( long propBlock )
    {
        this.propBlock[0] = propBlock;
        for ( int i = 1; i < this.propBlock.length; i++ )
        {
            this.propBlock[i] = 0;
        }
    }
    */
    public Collection<PropertyBlock> getPropertyBlocks()
    {
        return Collections.unmodifiableList( blockRecords );
    }

    public void removeBlock( int indexId )
    {

    }

    public int[] getKeyIndexIds()
    {
        int[] toReturn = new int[blockRecords.size()];
        int i = 0;
        for ( PropertyBlock block : blockRecords )
        {
            toReturn[i++] = block.getKeyIndexId();
        }
        return toReturn;
    }

    public void addPropertyBlock(PropertyBlock block)
    {
        if ( size() + block.getSize() > PropertyType.getPayloadSize() )
        {
            throw new IllegalStateException(
                    "Exceeded capacity of property record" );
        }
        blockRecords.add( block );
    }

    public PropertyBlock getPropertyBlock( PropertyIndex index )
    {
        return getPropertyBlock( index.getKeyId() );
    }

    public PropertyBlock getPropertyBlock( int keyIndex )
    {
        for ( PropertyBlock block : blockRecords )
        {
            if ( block.getKeyIndexId() == keyIndex )
            {
                return block;
            }
        }
        return null;
    }

    public long getNextProp()
    {
        return nextProp;
    }

    public void setNextProp( long nextProp )
    {
        this.nextProp = nextProp;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "PropertyRecord[" ).append( getId() ).append( "," ).append(
                inUse() ).append( "," ).append( "," )./*append( propBlock ).*/append(
                "," ).append( "," ).append( nextProp );
        buf.append( ", Value[" );
        Iterator<PropertyBlock> itr = blockRecords.iterator();
        while ( itr.hasNext() )
        {
            buf.append( itr.next() );
            if ( itr.hasNext() )
            {
                buf.append( ", " );
            }
        }
        buf.append( "]]" );
        return buf.toString();
    }

    public boolean isChanged()
    {
        return isChanged;
    }

    public void setChanged()
    {
        isChanged = true;
    }

    public long getPrevProp()
    {
        return prevProp;
    }

    public void setPrevProp( long prev )
    {
        prevProp = prev;
    }
}