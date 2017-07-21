package spim.process.interestpointremoval;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mpicbg.spim.data.sequence.ViewDescription;
import mpicbg.spim.data.sequence.ViewId;
import mpicbg.spim.data.sequence.VoxelDimensions;
import mpicbg.spim.io.IOFunctions;
import net.imglib2.KDTree;
import net.imglib2.RealPoint;
import net.imglib2.neighborsearch.KNearestNeighborSearchOnKDTree;
import spim.fiji.spimdata.SpimData2;
import spim.fiji.spimdata.interestpoints.InterestPoint;
import spim.fiji.spimdata.interestpoints.InterestPointList;
import spim.fiji.spimdata.interestpoints.ViewInterestPointLists;
import spim.fiji.spimdata.interestpoints.ViewInterestPoints;

public class ThinOut
{
	public static boolean thinOut( final SpimData2 spimData, final List< ViewId > viewIds, final ThinOutParameters top )
	{
		final ViewInterestPoints vip = spimData.getViewInterestPoints();

		final double minDistance = top.getMin();
		final double maxDistance = top.getMax();
		final boolean keepRange = top.keepRange();

		for ( final ViewId viewId : viewIds )
		{
			final ViewDescription vd = spimData.getSequenceDescription().getViewDescription( viewId );

			final ViewInterestPointLists vipl = vip.getViewInterestPointLists( viewId );
			final InterestPointList oldIpl = vipl.getInterestPointList( top.getLabel() );

			if ( oldIpl == null )
				continue;

			final VoxelDimensions voxelSize = vd.getViewSetup().getVoxelSize();

			// assemble the list of points (we need two lists as the KDTree sorts the list)
			// we assume that the order of list2 and points is preserved!
			final List< RealPoint > list1 = new ArrayList< RealPoint >();
			final List< RealPoint > list2 = new ArrayList< RealPoint >();
			final List< double[] > points = new ArrayList< double[] >();

			for ( final InterestPoint ip : oldIpl.getInterestPointsCopy() )
			{
				list1.add ( new RealPoint(
						ip.getL()[ 0 ] * voxelSize.dimension( 0 ),
						ip.getL()[ 1 ] * voxelSize.dimension( 1 ),
						ip.getL()[ 2 ] * voxelSize.dimension( 2 ) ) );

				list2.add ( new RealPoint(
						ip.getL()[ 0 ] * voxelSize.dimension( 0 ),
						ip.getL()[ 1 ] * voxelSize.dimension( 1 ),
						ip.getL()[ 2 ] * voxelSize.dimension( 2 ) ) );

				points.add( ip.getL() );
			}

			// make the KDTree
			final KDTree< RealPoint > tree = new KDTree< RealPoint >( list1, list1 );

			// Nearest neighbor for each point, populate the new list
			final KNearestNeighborSearchOnKDTree< RealPoint > nn = new KNearestNeighborSearchOnKDTree< RealPoint >( tree, 2 );
			final InterestPointList newIpl = new InterestPointList(
					oldIpl.getBaseDir(),
					new File(
							oldIpl.getFile().getParentFile(),
							"tpId_" + viewId.getTimePointId() + "_viewSetupId_" + viewId.getViewSetupId() + "." + top.getNewLabel() ) );

			final ArrayList< InterestPoint > newIPs = new ArrayList<>();

			int id = 0;
			for ( int j = 0; j < list2.size(); ++j )
			{
				final RealPoint p = list2.get( j );
				nn.search( p );
				
				// first nearest neighbor is the point itself, we need the second nearest
				final double d = nn.getDistance( 1 );
				
				if ( ( keepRange && d >= minDistance && d <= maxDistance ) || ( !keepRange && ( d < minDistance || d > maxDistance ) ) )
					newIPs.add( new InterestPoint( id++, points.get( j ).clone() ) );
			}

			newIpl.setInterestPoints( newIPs );
			newIpl.setCorrespondingInterestPoints( new ArrayList<>() );

			if ( keepRange )
				newIpl.setParameters( "thinned-out '" + top.getLabel() + "', kept range from " + minDistance + " to " + maxDistance );
			else
				newIpl.setParameters( "thinned-out '" + top.getLabel() + "', removed range from " + minDistance + " to " + maxDistance );

			vipl.addInterestPointList( top.getNewLabel(), newIpl );

			IOFunctions.println( new Date( System.currentTimeMillis() ) + ": TP=" + vd.getTimePointId() + " ViewSetup=" + vd.getViewSetupId() + 
					", Detections: " + oldIpl.getNumInterestPoints() + " >>> " + newIpl.getNumInterestPoints() );
		}

			return true;
	}

}