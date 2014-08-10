package explorviz.shared.experiment

import com.google.gwt.user.client.rpc.IsSerializable

class StatisticQuestion implements IsSerializable {
	String question
	String[] choices
	String tooltip
	String placeholder
	/** zero -> unlimited */
	int length
	/** zero -> unlimited */
	int min
	/** zero -> unlimited */
	int max
	Type type
	String id
	
	
	String openFormDiv = "<div class='form-group' id='form-group'>"
	String closeFormDiv = "</div>"
	
	/**
	 * @param input : the input that is parsed to build the question
	 * differs for different question types; \n seperates the different attributes
	 */
	new(String input, int ID){
		var inputs = input.split("\n")
		var t = inputs.get(0)
		id = "form"+ID.toString()
		if(t.startsWith("Combobox")){
			question = t.substring("Combobox:".length).trim()
			choices = inputs.get(1).substring("Choices:".length).split(",")
			tooltip = inputs.get(2).substring("Tooltip:".length).trim
			type = Type.CHOICES
		}else if(t.startsWith("Binary")){
			question = t.substring("Binary:".length).trim()
			choices = inputs.get(1).substring("Choices:".length).split(",")
			type = Type.BINARY
		}else if(t.startsWith("Comment")){
			question = t.substring("Comment: ".length).trim()
			type = Type.COMMENT
		}else if(t.startsWith("Input")){
			question = t.substring("Input: ".length).trim()
			placeholder = inputs.get(1).substring("Placeholder:".length).trim()
			var l = inputs.get(2).substring("Length:".length).trim()
			length = if(l.equals("")){0}else{Integer.parseInt(l)}
			type = Type.INPUT
		}else if(t.startsWith("Number")){
			question = t.substring("Number:".length).trim()
			placeholder = inputs.get(1).substring("Placeholder:".length).trim()
			var mi = inputs.get(2).substring("Min:".length).trim()
			min = if(mi.equals("")){0}else{Integer.parseInt(mi)}
			var ma = inputs.get(3).substring("Max:".length).trim()
			max = if(ma.equals("")){0}else{Integer.parseInt(ma)}
			type = Type.NUMBER
		}else if(t.startsWith("E-Mail")){
			question = t.substring("E-Mail:".length).trim()
			placeholder = inputs.get(1).substring("Placeholder:".length).trim()
			type = Type.EMAIL
		}else if(t.startsWith("Text")){
			question = t.substring("Text:".length).trim()
			type = Type.PURETEXT
		}
	}
	
	/**
	 * Default for serialization
	 */
	private new(){}
	
	/**
	 * Returns html code to be part of a form
	 */
	def getHTML(){
		if(type.equals(Type.COMMENT)){
			getCommentHTML()
		}else if(type.equals(Type.INPUT)){
			getInputHTML()
		}else if(type.equals(Type.NUMBER)){
			getNumberHTML()
		}else if(type.equals(Type.EMAIL)){
			getEMailHTML()
		}else if(type.equals(Type.BINARY)){
			getBinaryHTML()
		}else if(type.equals(Type.CHOICES)){
			getChoicesHTML()
		}else if(type.equals(Type.PURETEXT)){
			getTextHTML()
		}
	}
	
	private def getCommentHTML(){
		var sb = new StringBuilder()
		sb.append(openFormDiv)
		sb.append("<label for='"+id+"'>"+question+"</label>")
		sb.append("<textarea class='form-control closureTextarea' id='"+id+"' name='"+id+"'></textarea>")
		sb.append(closeFormDiv)
		return sb.toString()
	}
	
	private def getInputHTML(){
		var sb = new StringBuilder()
		sb.append(openFormDiv)
		sb.append("<label for='"+id+"'>"+question+"</label>")
		sb.append("<input type='text' class='form-control' id='"+id+"' name='"+id+"' ")
		if(length!=0){
			sb.append("maxlength='"+length+"' ")
		}
		if(!placeholder.equals("")){
			sb.append("placeholder='"+placeholder+"' ")
		}
		sb.append("required>")
		sb.append(closeFormDiv)
		return sb.toString()
	}
	
	private def getNumberHTML(){
		var sb = new StringBuilder()
		sb.append(openFormDiv)
		sb.append("<label for='"+id+"'>"+question+"</label>")
		sb.append("<input type='number' class='form-control' id='"+id+"' name='"+id+"' ")
		if(min!=0){
			sb.append("min='"+min+"' ")
		}
		if(max!=0){
			sb.append("max='"+max+"' ")
		}
		if(!placeholder.equals("")){
			sb.append("placeholder='"+placeholder+"' ")
		}
		sb.append("required>")
		sb.append(closeFormDiv)
		return sb.toString()
	}
	
	private def getEMailHTML(){
		var sb = new StringBuilder()
		sb.append(openFormDiv)
		sb.append("<label for='"+id+"'>"+question+"</label>")
		sb.append("<input type='email' class='form-control' id='"+id+"' name='"+id+"'")
		if(!placeholder.equals("")){
			sb.append("placeholder='"+placeholder+"' ")
		}
		sb.append("required>")
		sb.append(closeFormDiv)
		return sb.toString()
	}
	
	private def getBinaryHTML(){
		var sb = new StringBuilder()
		sb.append(openFormDiv)
		sb.append("<div id='radio' class='input-group'>")
		sb.append("<p>"+question+"</p>")
		sb.append("<input type='radio' id='"+id.toString()+"0"+"' name='"+id+"' value='"+choices.get(0).trim()+"' style='margin-left:10px;' required>
						<label for='"+id.toString()+"0"+"' style='margin-right:15px; margin-left:5px'>"+choices.get(0).trim()+"</label> ")
		sb.append("<input type='radio' id='"+id.toString()+"1"+"' name='"+id+"' value='"+choices.get(1).trim()+"' style='margin-left:10px;' required>
						<label for='"+id.toString()+"1"+"' style='margin-right:15px; margin-left:5px'>"+choices.get(1).trim()+"</label> ")
		sb.append("</div>")	
		sb.append(closeFormDiv)
		return sb.toString()
	}
	
	private def getChoicesHTML(){
		var sb = new StringBuilder()
		sb.append(openFormDiv)
		sb.append("<label for='"+id+"'>"+question+"</label>")
		if(!tooltip.equals("")){
			sb.append("<span class='glyphicon glyphicon-question-sign blueGlyph' data-container='body' data-html='true' data-toggle='popover' rel='popover' data-trigger='hover' data-placement='right' data-content='"+tooltip+"'></span>")
		}
		sb.append("<select class='form-control' id='"+id+"' name='"+id+"' required>")
		for(var i = 0; i<choices.length; i++){
			sb.append("<option>"+choices.get(i).trim()+"</option>")
		}
		sb.append("</select>")
		sb.append(closeFormDiv)
		return sb.toString()
	}
	
	private def getTextHTML(){
		return "<p>"+question+"</p>"
	}
	
}

enum Type{
	COMMENT, INPUT, NUMBER, EMAIL, BINARY, CHOICES, PURETEXT
}