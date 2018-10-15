UPDATE: Mentioned "link" below is now generated using a custom (user defined) javascript, just refering to offline disponible flickr thumbnails url (along with sizes, photo-title, main-url, etc). That allows to use img srcset attribute, adding a "nofollow" to links, etc. In two words: full flexibility.

--

# flickr-easy-share
Get flickr photo links, in html img and anchor tags format, without accesing flickr web site, and offline disponible.

Generates an html visualization of your public flickr pictures organized by albums using thumbnails for each. Each thumbnail will show an html portion of code with an img (in the size of your choice) linked to the flickr photo page using photo title as alt attribute for img. Something like this:

```
<a href="http://www.flickr.com/photos/user/photo-id/" title="Enlarge photo-title" target="_blank"><img title="Enlarge " alt="photo-title" width="320" height="180" src="http://farm8.staticflickr.com/7342/photo-id_a05770d49e_n.jpg"></a>
```

Where "photo-id" is the id of your photo (as 2016/06/10 always a number). And "farm8", "7342", "a05770d49e" and "n" are codes provided by flickr. "320" is the width that you selected for the link (see below), and "180" is the corresponding proportional height of 320 (see below). "photo-title" is the title that you set up, and "Enlarge" is a prefix configurable when you run this app.

After running, you will have an index.html pointing to other htmls representing your public albums and a special album containing public pictures that don't belong to any album. Each album will consist in html containing thumbnails (in the size of your choice) of each public picture on that album. These thumbnails will be downloaded (only in the choosen size) using a bash script (generated after running the application). This last action (download of thumbnails) is an intensive use of the connection (I am thinking of someone travelling, facing slow connections all the time), so its resumable, and you are not going to need to download these thumbnails again once you did. As counterpart, thumbnails are not going be updated in the case that you replaced the original picture (but img tags are going to be updated with new width and height).

In those albums, each thumbnail has, below, a textarea with an html code like the one shown above. Additionally you will see a combo with flickr sizes (75x75, 100x20, 150x150, 240, 320, 500, 640, 800, 1024, original. Notice: 1600 and 2048 are not available because of this [bug](https://github.com/callmeal/Flickr4Java/issues/178)). Selecting one size in that combo will change code in textareas. For example, in the html code quoted above, 320 was selected on the combo. If you selected 640 in that combo, that code will be something like:

```
<a href="http://www.flickr.com/photos/user/photo-id/" title="Enlarge photo-title" target="_blank"><img title="Enlarge " alt="photo-title" width="640" height="360" src="http://farm8.staticflickr.com/7342/photo-id_a05770d49e_z.jpg"></a>
```

This program saves the information downloaded, of photos, from flickr in the last use, avoiding retrieving all the information again. Next time, only updates and new photos are going to be retrieved. But flickr doesn't provide updates about deleted images, so they will remain there (but only visible from "Pictures that don't belong to any album"). As said, retrieved thumbnails are not going to be updated (if they were replaced) unless you delete them.

-

How to run?

```
mvn compile exec:java -Dexec.mainClass="org.github.fvsnippets.flickr_easy_html_share.App" -Dexec.args="'/path/to/config/file.xml'"
```

You will be prompted for configurations that will be saved on /path/to/config/file.xml, so you don't need to enter them again.
Those configurations include a flickr API KEY and the corresponding SHARED SECRET. Just go to https://www.flickr.com/services/apps/create/ and create a dummy (random name) app (if you don't have one yet); you will be provided with the data required.

Also this app required read access to your private contents (like private pictures), so you are going to need to authorize your dummy app (the one that you created in order to provide api key and shared secret) to have that permission.

After finishing run downloadPictures.sh (generated in the same directory that you set up for index.html and the albums view). It's a bash script. You are going to need an *nix system (like Linux) or something like cygwin (with wget available) installed on your m$ windows in order to run it.

-

Copyright (C) 2016 Federico Valido

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
