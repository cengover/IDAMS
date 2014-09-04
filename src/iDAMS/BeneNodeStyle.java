package iDAMS;
import java.awt.Color;
import java.awt.Font;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;

public class BeneNodeStyle implements Style3D<Bene> {
	
		
	Color tan = new Color(205, 133, 63);
		
	public TaggedBranchGroup getBranchGroup(Bene agent, TaggedBranchGroup taggedGroup) {
			
		if (taggedGroup == null || taggedGroup.getTag() == null) {
			taggedGroup = new TaggedBranchGroup("DEFAULT");
			Shape3D cube = ShapeFactory.createCube(.03f, "DEFAULT");

			Transform3D trans = new Transform3D();
	   		trans.set(new Vector3f(0, 0, -.05f));
	   		trans.setScale(new Vector3d(1, 1, 0.5));
	  		TransformGroup grp = new TransformGroup(trans);

	  		grp.addChild(cube);
			  taggedGroup.getBranchGroup().addChild(grp);

				return taggedGroup;
		}
			return null;
	}
		
		public float[] getRotation(Bene o) {
			return null;
		}
		
		public String getLabel(Bene o, String currentLabel) {
			return null; //return currentLabel.length() > 0 ? currentLabel : String.valueOf(o.getId());
//			return o.toString();
		}
		
		public Color getLabelColor(Bene t, Color currentColor) {
			return Color.YELLOW;
		}
		
		public Font getLabelFont(Bene t, Font currentFont) {
			return null;
		}
		
		public LabelPosition getLabelPosition(Bene o, LabelPosition curentPosition) {
			return LabelPosition.NORTH;
		}
		
		public float getLabelOffset(Bene t) {
			return .035f;
		}
		
		public TaggedAppearance getAppearance(Bene agent, TaggedAppearance taggedAppearance, Object shapeID) {
			if (taggedAppearance == null) {
				taggedAppearance = new TaggedAppearance();
			}

			AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), tan);
			return taggedAppearance;		
		}
		
		public float[] getScale(Bene o) {
			return null;
		}
}

