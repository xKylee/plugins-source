/*
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.runedoku;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;

class RunedokuUtil
{

	private final RunedokuConfig config;

	@Inject
	RunedokuUtil(final RunedokuConfig config)
	{
		this.config = config;
	}

	Color sudokuPieceToColor(int i)
	{
		switch (i)
		{
			case 1:
				return config.mindRuneColor();
			case 2:
				return config.fireRuneColor();
			case 3:
				return config.bodyRuneColor();
			case 4:
				return config.airRuneColor();
			case 5:
				return config.deathRuneColor();
			case 6:
				return config.waterRuneColor();
			case 7:
				return config.chaosRuneColor();
			case 8:
				return config.earthRuneColor();
			case 9:
				return config.lawRuneColor();
			default:
				return Color.RED;
		}
	}

	Color referenceColors(int i)
	{
		switch (i)
		{
			case 1: //water
				return config.waterRuneColor();
			case 2: //fire
				return config.fireRuneColor();
			case 3: //earth
				return config.earthRuneColor();
			case 4: //air
				return config.airRuneColor();
			case 5: //mind
				return config.mindRuneColor();
			case 6: //body
				return config.bodyRuneColor();
			case 7: //plugin
				return config.lawRuneColor();
			case 8: //chaos
				return config.chaosRuneColor();
			case 9: //death
				return config.deathRuneColor();
			default:
				return Color.RED;
		}
	}

	/**
	 * Make the 2d array into an arraylist
	 *
	 * @param board
	 * @return
	 */
	List<Integer> makeSimple(int[][] board)
	{
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 9; i++)
		{
			for (int ii = 0; ii < 9; ii++)
			{
				list.add(board[i][ii]);
			}
		}
		return list;
	}

	/**
	 * utility method
	 *
	 * @param rect
	 * @return
	 */
	static Polygon rectangleToPolygon(Rectangle rect)
	{
		int[] xpoints = {rect.x, rect.x + rect.width, rect.x + rect.width, rect.x};
		int[] ypoints = {rect.y, rect.y, rect.y + rect.height, rect.y + rect.height};
		return new Polygon(xpoints, ypoints, 4);
	}

	/**
	 * Pulls data from what's on the Runedoku interface and creates a 2dimensional array for it
	 *
	 * @param client
	 * @return sudoku table that the client currently sees in a 2d array
	 * @author gazivodag
	 */
	int[][] createTable(Client client)
	{
		int[][] myArr = new int[9][9];
			Widget sudokuScreen = client.getWidget(292, 13);
		for (int i = 0; i < 9; i++)
		{
			for (int ii = 0; ii < 9; ii++)
			{
				int item;
				int myIndex;
				if (i > 0)
				{
					myIndex = ((i * 10) + ii) - i;
				}
				else
				{
					myIndex = ii;
				}
				if (myIndex == 81)
				{
					break;
				}
				item = sudokuScreen.getChild(myIndex).getItemId();
				if (item > 0)
				{
					myArr[i][ii] = Objects.requireNonNull(RunedokuPiece.getById(item)).getPieceForSudoku();
				}
				else
				{
					myArr[i][ii] = 0;
				}
			}
		}
		return myArr;
	}

	/**
	 * @param client
	 * @return
	 */
	int getSelectedPiece(Client client)
	{

			Widget selectedPieceWidget = client.getWidget(292, 9);
			if (!selectedPieceWidget.isHidden())
			{
				for (int i = 1; i < 10; i++)
				{
					if (selectedPieceWidget.getChild(i).getBorderType() == 2)
					{
						return RunedokuPiece.getById(selectedPieceWidget.getChild(i).getItemId()).getPieceForSudoku();
					}
				}
			}
		return -1;
	}

}
