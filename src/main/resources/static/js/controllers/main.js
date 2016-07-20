app.controller('MainController', function($scope, $rootScope, $http, $state){
	
	$scope.toHome = function(){
		$state.transitionTo($state.current, null, { 
			  reload: true, inherit: false, notify: true
			});
	}
	
    $rootScope.authenticate = function(credentials, callback) {
        var headers = credentials ? {
            authorization : "Basic "
            + btoa(credentials.username + ":"
                + credentials.password)
        } : {};
        $http.get('user', {
            headers : headers
        }).success(function(data) {
            if (data.name) {
                $rootScope.authenticated = true;
                $rootScope.username = data.name;
                $rootScope.authority = data.authorities[0].authority;
            } else {
                $rootScope.authenticated = false;
            }
            callback && callback($rootScope.authenticated);
        }).error(function() {
            $rootScope.authenticated = false;
            callback && callback(false);
        });

    };
    $rootScope.authenticate();
    $rootScope.credentials = {};
});