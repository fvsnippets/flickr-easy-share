# flickr-easy-share

Get flickr photo links, in HTML **&lt;img&gt;** and **&lt;a&gt;** tags format, without accessing flickr web site, and offline available.

Generates an HTML visualization of your **public** flickr *pictures*, organized by *albums*, and using *thumbnails* for each one. Each *thumbnail* will show an HTML portion of code with an **&lt;img&gt;** (in the size of your choice) linked to the flickr *picture* page using picture *title* as **alt** attribute for **&lt;img&gt;**. Something like this:

```html
<a href="http://www.flickr.com/photos/user/my-photo-id/" title="Expand my-photo-title" target="_blank"><img title="my-photo-title" alt="my-photo-title" width="320" height="183" src="http://farm8.staticflickr.com/7342/my-photo-id_a05770d49e_n.jpg"></a>
```

Where "**my-photo-id**" is the id of your picture (as of 2016/06/10 is always a number). And "**farm8**", "**7342**", "**a05770d49e**" and "**n**" are codes provided by flickr. "**320**" is the *width* that you selected for the link (see below), and "**183**" is the corresponding proportional *height* for the specific picture (see below). Finally "**my-photo-title**" and "**Expand my-photo-title**" are texts that you may choose referring to the *title* of your picture.  
The mentioned HTML portion of code is just and example, and is fully configurable using all the offline flickr available data.

After running, you will found an *index.html* file pointing to *HTMLs* files representing your public **albums** and a **special album** containing public **pictures** that don't belong to any **album**. Each **album** will consist in an HTML file containing **thumbnails** (in the **size** of your choice) of each public **picture** on that **album**. These **thumbnails** will be downloaded (only in the choosen **size**) using a *bash* script (generated after running the application). That last action (download of **thumbnails**) will make an intensive use of the connection (I am thinking of someone travelling, facing slow connections all the time), so its resumable, and you are not going to need to download these **thumbnails** again once you did. As a counterpart, a **thumbnail** is not going be updated in case that you replaced the original **picture** (but *&lt;img&gt;* tags are going to be updated with new *width* and *height*).

In those *albums*, each *thumbnail* will have, aside, a **&lt;textarea&gt;** with an HTML code like the one shown above.  
That HTML code is generated using a custom (user defined) **javascript** refering to offline disponible flickr *thumbnails-url* (along with *sizes*, *photo-title*, *main-url*, etc). That allows to use **&lt;img&gt;**'s **srcset** attribute, adding a **nofollow** to links, etc. In two words: full flexibility.  
For example, the html code shown above was generated using:

```javascript
'<a href="' + pictureUrl + '" title="Expand ' + pictureTitle + '"><img title="' + pictureTitle + '" alt="' + pictureTitle + '" src="' + url_N + '" widht="' + width_N + '" height="' + height_N + '" /></a>'
```

Available offline variables and examples are explained in the generated HTML album pages, but in a nutshell they are: **pictureUrl** ; **pictureTitle** ; **url_${size}** ; **width_${size}** ; **height_${size}**; with *${size}* being one of **SQ** (75x75), **T** (100x20), **Q** (150x150), **S** (240), **N** (320), **M** (500), **Z** (640), **C** (800), **L** (1024), **H** (1600), **K** (2048), **O** (Original). 

This program saves the *downloaded* information of photos from flickr in the last use, avoiding retrieving all the information again. Next time, only **updated** and **new** photos are going to be retrieved. But flickr doesn't provide (as of 2016/06/10) updates about **deleted** images, so they will remain there (but only visible in the "album" named "Pictures that don't belong to any album"). As said, retrieved *thumbnails* are not going to be updated (if they were replaced) unless you delete them.

-

How to run?

```
mvn compile exec:java -Dexec.mainClass="org.github.fvsnippets.flickr_easy_html_share.App" -Dexec.args="'/path/to/config/file.xml'"
```

Also an *script* including that command is available: **invoker.sh**. It requires *bash* and will set **"/path/to/config/file.xml"** to **${HOME}/flickr-share/.flickr-easy-share**.

The first time you run flickr-easy-share, you will be prompted for *configurations* that will be saved on the **/path/to/config/file.xml**, so you don't need to enter them again.
Those configurations include a flickr **API KEY** and the corresponding **SHARED SECRET**. Just go to *https://www.flickr.com/services/apps/create/* and create a dummy (random name) **app** (if you don't have one yet); you will be provided with the data required.

Also this app requires **read** access to your private contents (like private pictures; but this app won't access them... you can see the code if you don't trust me), so you are going to need to **authorize** your dummy app (the one that you created in order to provide *API KEY* and *SHARED SECRET*) to have that **permission**.

After finishing, then please run **downloadPictures.sh**. You will find it in the same directory that you set up for *index.html* and the *albums* view. It's a *bash* script. You are going to need an *unix* like system (such as *Linux* or *macOS*) or something like *cygwin* (with *wget* available) installed on your *M$ Windows* in order to run it.

-

Copyright (C) 2016-2020 Federico Valido

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
