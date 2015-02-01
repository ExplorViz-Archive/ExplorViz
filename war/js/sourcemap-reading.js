( function($) {
	jQuery(document).ready(function() {  
		var SourceMapReader = {
		    sourcemap: '',
		    cache: null,
		    sourceMeta: null,
		    realOutput: '',
		    files: [],
		    filenames: [],

		    readMapURL: function(mapURL){
		    	SourceMapReader.get(mapURL, function(json){
					SourceMapReader.filenames = json.sources;
					SourceMapReader.sourcemap = json.mappings;
					
					SourceMapReader.files = [];
//					json.sources.forEach(function(source, i){
//						SourceMapReader.get(json.sourceRoot + source, function(str){
						// files can arrive in any order, but we dont really care about that.
						// last one stays on screen until user clicks on a segment in the source.
//						SourceMapReader.files[i] = str;
						//SourceMapReader.dom.sourcename.innerHTML = ' ('+source+')';
//					  });
//					});
		
					SourceMapReader.cacheMap();
					
					SourceMapReader.sourceMeta = [];
				});
		    },
		    /**
		     * Compute the absolute positions of each segment. This eases the lookup times
		     * at runtime because all segments are relative. That means in order to know the
		     * position of the very last segment, you need to run through all the previous
		     * segments first.
		     * (While this could be delegated to run on-demand, and only up to the point where
		     * you need it, I chose not to do that for simplicity.)
		     *
		     * The data is cached and when the generated source UI is built, the absolute
		     * values are set as an attribute. After that, the cache is no longer used as
		     * it will immediately use just that absolute file/col/row value and act accordingly.
		     */
		    cacheMap: function(){
		      var cache = SourceMapReader.cache = [];
		      var tcol = 0;
		      var trow = 0;
		      var scol = 0;
		      var srow = 0;
		      var file = 0;
		      SourceMapReader.sourcemap.split(';').forEach(function(line,i){
		        tcol = 0;
		        line.split(',').forEach(function(str){
		          var segment = SourceMapReader.decodeSegment(str);
		          // 0: target col delta, 1: file index, 2: source row delta, 3: source col delta, 4: name index, 5: chunk string
		          cache.push({
		            segment: segment,
		
		            tcol: tcol+=segment[0]|0,
		            trow: i,
		
		            scol: scol+=segment[3]|0,
		            srow: srow+=segment[2]|0,
		
		            file: file+=segment[1]|0,
		          });
		        });
		      });
		    },
		    /**
		     * Decode given string, which should be a segment in a sourcemap.
		     * A segment consists of one, four, or five Base64 VLQ encoded
		     * numbers, which are all delta's, and which are fields of the
		     * source map.
		     *
		     * Base64 is a simple mapping of 0-63 to: A-Za-z0-9+/
		     *
		     * VLQ is "variable length quantifier" and deemed an efficient way
		     * to encode small numbers.
		     *
		     * This function will return an array with each field that was
		     * encountered. The fields are: [target pos, source file index,
		     * target row, target col, name index]. All fields are delta,
		     * relative to the previous field (first field offsets at zero).
		     *
		     * @param {string} segment
		     * @return {number[]} One, four, or five fields
		     */
		    decodeSegment: function(segment){
		      // decode the VLQ encoded segment. 1, 3, or 4 fields
		      var bytes = this.decodeBase64(segment);
		
		      var fields = [];
		      while (bytes.length) fields.push(SourceMapReader.decodeOneNumberVQL(bytes));
		
		      fields[5] = segment; // debug
		
		      return fields;
		    },
		    /**
		     * Very straightforward base64 decoder.
		     *
		     * @param {string} str
		     * @return {number[]}
		     */
		    decodeBase64: function(str){
		      return str.split('').map(function(b){
		        // each byte is base64 encoded
		        return 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'.indexOf(b);
		      });
		    },
		    /**
		     * Decode one signed number in a VQL. Will remove used items from
		     * the input array.
		     *
		     * @param {number[]} bytes
		     * @return {number}
		     */
		    decodeOneNumberVQL: function(bytes){
		      var num = 0;
		      var n = 0;
		      do {
		        var b = bytes.shift();
		        num += (b & 31) << (5*n++);
		      } while (b>>5);
		      var sign = num & 1;
		      num >>= 1;
		      if (sign) num = -num;
		
		      return num;
		    },
		    get: function(url, func){
		    	jQuery.get(url, func);
			 },
			 lookupFilename: function(row){
				for (var i = 0; i < SourceMapReader.cache.length; i++) {
					var cacheEntry = SourceMapReader.cache[i];
					if (cacheEntry.trow == row) {
						return SourceMapReader.filenames[cacheEntry.file];
					}
		         };
				return null;
			 }, 
		  };
		
		jQuery.fn.readSourceMapURL = function(url) {
			SourceMapReader.readMapURL(url);
		};
		
		jQuery.fn.lookupFilename = function(row) {
			return SourceMapReader.lookupFilename(row);
		};
		
		
	});
} ) ( jQuery );