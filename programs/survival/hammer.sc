//!scarpet v1.5

// hammer mode is activated by
// - main hand holds a pickaxe
// - off hand holds diamond(s)
// 
// shift + right click in hammer mode can change the range
// the range is cycle among 1x1, 3x3, and 5x5 (indicated by red dust particle)
// the range is indicated by red dust particle when changing the range and breaking blocks
//
// note that sneaking will temporarily turn off hammer mode to break just one block

// stay loaded
__config() -> (
	m(
		l('stay_loaded','true')
	)
);

__get_block_from_to(position, face) ->
(
	// print('> face: '+face);
	// print('> global_radius: '+global_radius);
	l(x,y,z) = position;
	r = global_radius;
	block_from_to = if (
		face == 'up',    l(l(x+r+1, y+1,   z+r+1), l(x-r, y+1, z-r)),
		face == 'down',  l(l(x+r+1, y,     z+r+1), l(x-r, y,   z-r)),
		face == 'north', l(l(x+r+1, y+r+1, z),     l(x-r, y-r, z)),
		face == 'south', l(l(x+r+1, y+r+1, z+1),   l(x-r, y-r, z+1)),
		face == 'east',  l(l(x+1,   y+r+1, z+r+1), l(x+1, y-r, z-r)),
		face == 'west',  l(l(x,     y+r+1, z+r+1), l(x,   y-r, z-r))
	);
	// print('> block_from_to: '+block_from_to);
	return(block_from_to)
);

__get_block_range(position, face) -> 
(
	l(x,y,z) = position;
	block_range = if (
		face == 'up' || face == 'down', rect(x,y,z,global_radius,0,global_radius ),
		face == 'north' || face == 'south', rect(x,y,z,global_radius,global_radius,0),
		face == 'east'  || face == 'west', rect(x,y,z,0,global_radius,global_radius)
	);
	return(filter(block_range, __harvestable(_)))
);
__harvestable(block) -> !(air(block) || (block == 'lava') || (block == 'water'));

global_radius = 1;
global_breakrange = null;

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if (hand != 'mainhand' || !item_tuple || !(item_tuple:0 ~'_pickaxe'), return());
	if (!(player ~ 'sneaking'), return());
	if (player ~ ['holds', 'offhand']:0 != 'diamond', return());

	global_radius = (global_radius + 1)%3;
	diameter = 2*global_radius+1;

	// print('hammer mode '+diameter+'x'+diameter);

	l(from, to) = __get_block_from_to(pos(block), face);
	particle_box('dust 0.8 0.1 0.1 1', from, to, 0.1)
);

__on_player_clicks_block(player, block, face) ->
(
	global_breakrange = null;

	if (!global_radius, return());
	if (player ~ 'sneaking', return());
	if (!(player ~ ['holds', 'mainhand']:0 ~ '_pickaxe'), return());
	if (player ~ ['holds', 'offhand']:0 != 'diamond', return());

	global_breakrange = __get_block_range(pos(block), face);
	l(from, to) = __get_block_from_to(pos(block), face);
	particle_box('dust 0.8 0.1 0.1 1', from, to, 0.5)
);

__on_player_breaks_block(player, block) -> 
(
	block_range = global_breakrange;
	global_breakrange = null;

	if (!global_radius || !block_range, return());
	if (player ~ 'sneaking', return());
	if (!(player ~ ['holds', 'mainhand']:0 ~ '_pickaxe'), return());

	for(block_range, harvest(player, _))
)
