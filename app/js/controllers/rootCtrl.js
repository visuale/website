angular.module('jboss-forge').controller('rootCtrl', function($scope){
	$scope.$on('$viewContentLoaded', function(event) {
		initializeUI();
	});
});