package explorviz.plugin.capacitymanagement;

public class CapManClientSideJS {
	public static native void openPlanExecutionQuestionDialog(String warningText,
			String countermeasureText, String consequenceText) /*-{
		$wnd.jQuery("#planExecutionQuestionDialog").show();
		$wnd
				.jQuery("#planExecutionQuestionDialog")
				.dialog(
						{
							closeOnEscape : true,
							modal : true,
							resizable : false,
							title : 'Capacity Adjustment Suggestion',
							width : 500,
							height : 430,
							position : {
								my : 'center center',
								at : 'center center',
								of : $doc
							},
							buttons : {
								"OK" : function() {
									@explorviz.plugin.capacitymanagement.CapManClientSide::conductOkAction()()
									$wnd.jQuery(this).dialog("close");
								},
								"Refine manually" : function() {
									@explorviz.plugin.capacitymanagement.CapManClientSide::conductManualRefinementAction()()
									$wnd.jQuery(this).dialog("close");
								},
								"Cancel" : function() {
									@explorviz.plugin.capacitymanagement.CapManClientSide::conductCancelAction()()
									$wnd.jQuery(this).dialog("close");
								}
							}
						}).focus();

		$doc.getElementById("planExecutionQuestionDialog").innerHTML = "<div style='font-weight:bold;font-size:120%;'>Warning</div><p style='padding-left:10px;padding-bottom:10px;'>"
				+ warningText
				+ "</p><div style='font-weight:bold;font-size:120%;'>Countermeasure</div><p style='padding-left:10px;padding-bottom:10px;'>"
				+ countermeasureText
				+ "</p><div style='font-weight:bold;font-size:120%;'>Consequence</div><p style='padding-left:10px;padding-bottom:10px;'>"
				+ consequenceText
				+ "</p><p>Directly execute the adjustment?</p>";
	}-*/;
}
