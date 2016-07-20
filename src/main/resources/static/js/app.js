var app = angular.module('app', ['ngAnimate','ngSanitize','ngMessages','ui.router','ui.bootstrap']);

app.run(function($rootScope){
	$rootScope.authenticated = false;
    $rootScope.title = "Закупки";
});

app.config(function($stateProvider, $urlRouterProvider, $httpProvider) {
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    $stateProvider
        .state('purchase', {
            url: '/purchase/{id}',
            templateUrl : 'pages/purchase_detail.html',
            controller : 'PurchaseDetailController'
        })
        .state('search',{
            url : '/search/?id&quick&fz&query&customerDialog&corder&cpage&corderby&hideFilter&order&orderby&purchaseName&customer&minPrice&maxPrice&category&type&completed&startDate&endDate/{page}',
        	templateUrl : 'pages/advanced_search.html',
            controller  : 'AdvancedSearchController'
        })
        .state('registration',{
            url : '/registration/',
        	templateUrl : 'pages/registration.html',
            controller  : 'RegistrationController'
        })
        .state('verification',{
            url : '/verify/{token}',
        	templateUrl : 'pages/verification.html',
            controller  : 'VerificationController'
        })
        .state('recovery-email',{
            url : '/recovery-email/',
        	templateUrl : 'pages/recovery.html',
            controller  : 'RecoveryController'
        })
        .state('recovery-pas',{
            url : '/recovery/{token}',
        	templateUrl : 'pages/recovery-pas.html',
            controller  : 'RecoveryPasController'
        })
        .state('fav',{
            url : '/fav/{page}?order&orderby',
            templateUrl : 'pages/favorites.html',
            controller  : 'FavController'
        })
        .state('subscribe',{
            url : '/subscribe/',
            templateUrl : 'pages/subscribe-form.html',
            controller  : 'SubscribeController'
        })
        .state('admin-pattern',{
            url : '/admin/pattern/',
            templateUrl : 'pages/admin-pattern.html',
            controller  : 'AdminPatternController'
        })
        .state('downloads',{
            url : '/downloads/',
            templateUrl : 'pages/downloads.html',
            controller  : 'DownloadsController'
        })
        .state('properties',{
            url : '/properties/',
            templateUrl : 'pages/properties.html',
            controller  : 'PropertiesController'
        })
        .state('error',{
            url : '/error/?message&code',
            templateUrl : 'pages/error.html',
            controller  : 'ErrorController'
        });
	$urlRouterProvider.when('/fav/','/fav/1');
	$urlRouterProvider.otherwise('/search/?hideFilter=true&completed=false');
});

app.directive('purchases', function () {
	return {
	    templateUrl: 'pages/purchase_list_template.html'
	}
});

app.directive("compareTo", function(){
	return {
        require: "ngModel",
        scope: {
            otherModelValue: "=compareTo"
        },
        link: function(scope, element, attributes, ngModel) {
             
            ngModel.$validators.compareTo = function(modelValue) {
                return modelValue == scope.otherModelValue;
            };
 
            scope.$watch("otherModelValue", function() {
                ngModel.$validate();
            });
        }
    };
});

app.factory('paginate', function () {
    return function ($scope,$http,$state,$stateParams){
        $scope.pageSize = 15;
        $scope.totalItems = 100000;
        $scope.url+=$scope.pageSize;
        getPage($scope.currentPage);
        function getPage(page){
            $http.get($scope.url+'&page='+(page-1))
                .success(function(data) {
                    $scope.purchases = data.content;
                    $scope.totalItems = data.totalElements;
                });
        }
        $scope.pageChanged = function(){
            getPage($scope.currentPage);
            if($scope.cpage == true )
                $stateParams.cpage = $scope.currentPage;
            else
                $stateParams.page = $scope.currentPage;
            $state.transitionTo($state.current.name, $stateParams, { notify: false });
        };
    }
});

app.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

app.service('fileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function(file, uploadUrl,success,error){
        var fd = new FormData();
        fd.append('file', file);
        $http.post(uploadUrl, fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            })
            .success(success)
            .error(error);
    }
}]);