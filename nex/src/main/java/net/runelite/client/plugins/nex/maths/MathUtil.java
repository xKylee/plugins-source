package net.runelite.client.plugins.nex.maths;

import net.runelite.api.coords.WorldPoint;

public class MathUtil
{
	public static double[] unitVec(WorldPoint a, WorldPoint b)
	{
		var x = b.getX() - a.getX();
		var y = b.getY() - a.getY();
		var m = Math.hypot(x, y);

		return new double[]{x / m, y / m};
	}

	public static double cosineSimilarity(double[] vectorA, double[] vectorB)
	{
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.length; i++)
		{
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}

		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}
}
