

justeb = [241, 419, 5990, 6461, 6488, 6765, 8887, 9061, 9322, 11542, 14961, 15660, 16147, 18667, 19336, 19994, 21484, 21780, 24458, 25253, 33652, 36381, 37302, 40047, 40051, 44704, 45963, 47213, 48791, 49719, 50388, 50438, 53146, 58254, 58943, 59285, 60499, 66874, 67639, 67835, 68139, 69754, 71327, 71776, 73497, 74502, 74512, 76966, 77742, 79105, 79393, 82351, 82930, 84533, 85548, 86117, 87957, 92061, 94472, 94550, 94571, 99898, 100297, 102704, 104360, 104880, 107090, 107235, 109197, 111393, 112020, 112337, 112908, 114091, 115709, 116342, 118122, 118478, 119229, 120982, 121622, 121987, 122548, 122934, 123659, 128149, 131453, 132095, 133560, 135344, 136071, 137367, 138605, 138882, 142903, 145020, 145286, 147599, 148187, 149222, 149231, 149727, 150900, 151205, 152590, 153679, 154351, 155291, 155480, 155586, 188527, 190826, 191703, 193036, 194132, 196423, 196501, 198119, 198822, 200395, 201069, 202850, 204758, 206088, 206880, 207754, 208329, 209768, 210280, 210583, 211644, 213012, 213347, 213555, 214379, 214380, 214772, 216404, 217014, 218585, 218700, 219005, 219776, 222707, 225618, 226943, 228812, 235217, 235601, 238004, 238094, 238602, 243894, 244530, 246295, 246350, 252195, 255234, 255235, 255710, 258932, 260011, 260628, 260724, 261790, 264447, 264955, 267669, 268658, 270025, 270297, 271418, 274851, 275242, 275714, 276280, 279167, 279577, 281356, 281859, 283356, 283959, 284367, 285575, 286735, 288189, 288435, 292605, 292801, 294540, 295047, 297518, 298666]

newcuts = [419, 5990, 6765, 9061, 9322, 14961, 15660, 16147, 17245, 19336, 21484, 24458, 24721, 25253, 36381, 37302, 40047, 40051, 44704, 46938, 48757, 48791, 50438, 53146, 58254, 58762, 58943, 59086, 66874, 67639, 67835, 69754, 73497, 77567, 79105, 79393, 82351, 86117, 87957, 92061, 94472, 94550, 94559, 94571, 99898, 100297, 104360, 104880, 107235, 109197, 112020, 112908, 114091, 115709, 116342, 118122, 119229, 120982, 121987, 123215, 123659, 123900, 128149, 131453, 132037, 132095, 137367, 138882, 142903, 145286, 145366, 147599, 149222, 149231, 150900, 152590, 154351, 155291, 155480, 155586, 188527, 190826, 191703, 193036, 194132, 196423, 196501, 198822, 199490, 202850, 204758, 206880, 208329, 209768, 210583, 211644, 213347, 214379, 218700, 219005, 224231, 225618, 231991, 238004, 238094, 241839, 243894, 252195, 254924, 255234, 255235, 258932, 260724, 270297, 274851, 275242, 275714, 279167, 279577, 281356, 281859, 282826, 283356, 283959, 284367, 285575, 292605, 294540, 297518, 298666]

for element in newcuts:
    #print(element)
    if element not in justeb:
       print(element)