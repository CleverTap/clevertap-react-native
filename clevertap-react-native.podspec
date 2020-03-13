require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name           = package['name']
  s.version        = package['version']
  s.summary        = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = package['homepage']
  s.source         = { :git => 'https://github.com/CleverTap/clevertap-react-native.git', :tag => s.version }

  s.requires_arc   = true
  s.module_name    = 'CleverTapReact' 
  s.platform       = :ios, '8.0'

  s.preserve_paths = 'LICENSE.md', 'README.md', 'package.json', 'index.js'
  s.source_files   = 'ios/CleverTapReact/*.{h,m}'

  s.dependency 'CleverTap-iOS-SDK', '~> 3.7.3'
  s.dependency 'React'
end
