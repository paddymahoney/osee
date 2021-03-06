/**
 * Agile Config Controller
 */
angular
		.module('AgileApp')
		.controller(
				'TeamCtrl',
				[
						'$scope',
						'AgileFactory',
						'$resource',
						'$window',
						'$modal',
						'$filter',
						'$routeParams',
						'LayoutService',
						'PopupService',
						function($scope, AgileFactory, $resource, $window,
								$modal, $filter, $routeParams, LayoutService,
								PopupService) {

							// ////////////////////////////////////
							// Agile Team table
							// ////////////////////////////////////
							$scope.selectedTeams = [];

							var configTeamTmpl = '<button class="btn btn-default btn-sm" ng-click="configTeam(row.entity)">Config</button>';
							var openBacklogImpl = '<button class="btn btn-default btn-sm" ng-click="openBacklog(row.entity)">Backlog</button>';

							$scope.teamGridOptions = {
								data : 'teams',
								enableHighlighting : true,
								enableColumnResize : true,
								multiSelect : false,
								showFilter : true,
								sortInfo : {
									fields : [ 'name' ],
									directions : [ 'asc' ]
								},
								columnDefs : [ {
									field : 'uuid',
									displayName : 'Id',
									width : 50
								}, {
									field : 'name',
									displayName : 'Name',
									width : 290
								}, {
									field : "backlog",
									displayName : 'Backlog',
									width : 66,
									cellTemplate : openBacklogImpl
								}, {
									field : "config",
									displayName : 'Config',
									width : 60,
									cellTemplate : configTeamTmpl
								} ]
							};

							$scope.updateTeams = function() {
								$scope.sheets = null;
								var loadingModal = PopupService
								.showLoadingModal();
								AgileFactory.getTeams().$promise
										.then(function(data) {
											$scope.teams = data;
											loadingModal.close();
											LayoutService
													.resizeElementHeight("teamTable");
											LayoutService.refresh();
										});
							}

							$scope.configTeam = function(team) {
								window.location.assign("main#/config?team="
										.concat(team.uuid))
							}

							$scope.openBacklog = function(team) {
								window.location.assign("main#/backlog?team="
										.concat(team.uuid))
							}

							$scope.addNewTeam = function() {
								var modalInstance = $modal.open({
									templateUrl : 'addNewTeam.html',
									controller : AddNewTeamModalCtrl,
								});

								modalInstance.result.then(function(teamName) {
									AgileFactory.addNewTeam(teamName).$promise
											.then(function(data) {
												$scope.updateTeams();
											});
								});
							}

							var AddNewTeamModalCtrl = function($scope,
									$modalInstance) {

								$scope.newTeam = {
									name : ""
								};

								$scope.ok = function() {
									$modalInstance.close($scope.newTeam.name);
								};

								$scope.cancel = function() {
									$modalInstance.dismiss('cancel');
								};
							};

							$scope.refresh = function() {
								$scope.updateTeams();
							}

							$scope.refresh();

						} ]);
